package Learning.Management.System.service;

import Learning.Management.System.dto.*;
import Learning.Management.System.entity.*;
import Learning.Management.System.repository.CourseRepo;
import Learning.Management.System.repository.EnrollmentRepo;
import Learning.Management.System.repository.QuizAttemptRepo;
import Learning.Management.System.repository.QuizRepo;
import Learning.Management.System.repository.UserRepo;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class QuizService {

    private final QuizRepo quizRepo;
    private final QuizAttemptRepo attemptRepo;
    private final CourseRepo courseRepo;
    private final EnrollmentRepo enrollmentRepo;
    private final UserRepo userRepo;

    public List<QuizCourseOptionDTO> getTeacherCourses(Authentication authentication) {
        User teacher = getRequester(authentication);
        if (teacher.getRole() != Role.TEACHER) {
            throw new AccessDeniedException("Only teachers can create quizzes");
        }
        return courseRepo.findByTeacherIdOrderByNameAsc(teacher.getId()).stream()
                .map(course -> QuizCourseOptionDTO.builder()
                        .id(course.getId())
                        .name(course.getName())
                        .code(course.getCode())
                        .build())
                .toList();
    }

    public List<QuizResponseDTO> getQuizzes(Authentication authentication) {
        User requester = getRequester(authentication);
        List<Quiz> quizzes;
        if (requester.getRole() == Role.ADMIN) {
            quizzes = quizRepo.findAll();
        } else if (requester.getRole() == Role.TEACHER) {
            quizzes = quizRepo.findByCourseTeacherIdOrderByOpensAtAsc(requester.getId());
        } else if (requester.getRole() == Role.LEARNER) {
            quizzes = quizRepo.findVisibleToLearner(requester.getId());
        } else {
            throw new AccessDeniedException("Only teachers, learners, and administrators can access quizzes");
        }
        return quizzes.stream().map(quiz -> mapQuiz(quiz, requester, false)).toList();
    }

    public QuizResponseDTO getQuiz(Long quizId, Authentication authentication) {
        User requester = getRequester(authentication);
        Quiz quiz = findQuiz(quizId);
        requireQuizAccess(quiz, requester);
        OffsetDateTime now = OffsetDateTime.now();
        boolean quizOpen = !now.isBefore(quiz.getOpensAt()) && !now.isAfter(quiz.getClosesAt());
        boolean answersReleased = !now.isBefore(quiz.getClosesAt());
        boolean includeQuestions = requester.getRole() != Role.LEARNER || quizOpen || answersReleased;
        return mapQuiz(quiz, requester, includeQuestions);
    }

    public QuizResponseDTO createQuiz(QuizRequestDTO dto, Authentication authentication) {
        User teacher = getRequester(authentication);
        Course course = courseRepo.findById(dto.getCourseId())
                .orElseThrow(() -> new EntityNotFoundException("Course not found"));
        if (course.getTeacher() == null || !course.getTeacher().getId().equals(teacher.getId())) {
            throw new AccessDeniedException("You can only create quizzes for your own courses");
        }
        if (!dto.getClosesAt().isAfter(dto.getOpensAt())) {
            throw new IllegalArgumentException("Quiz closing time must be after its opening time");
        }

        Quiz quiz = Quiz.builder()
                .course(course)
                .title(dto.getTitle().trim())
                .description(dto.getDescription())
                .opensAt(dto.getOpensAt())
                .closesAt(dto.getClosesAt())
                .questions(new ArrayList<>())
                .build();
        Map<QuizQuestion, Integer> correctOptionIndexes = new IdentityHashMap<>();
        for (QuizQuestionRequestDTO questionDto : dto.getQuestions()) {
            validateQuestion(questionDto);
            QuizQuestion question = QuizQuestion.builder()
                    .quiz(quiz)
                    .prompt(questionDto.getPrompt().trim())
                    .maxPoints(questionDto.getMaxPoints())
                    .questionType(questionDto.getQuestionType())
                    .imageUrl(questionDto.getImageUrl())
                    .options(new ArrayList<>())
                    .build();
            if (questionDto.getQuestionType() == QuizQuestionType.MULTIPLE_CHOICE) {
                for (int i = 0; i < questionDto.getOptions().size(); i++) {
                    question.getOptions().add(QuizQuestionOption.builder()
                            .question(question)
                            .optionText(questionDto.getOptions().get(i).trim())
                            .optionOrder(i)
                            .build());
                }
                correctOptionIndexes.put(question, questionDto.getCorrectOptionIndex());
            }
            quiz.getQuestions().add(question);
        }
        Quiz savedQuiz = quizRepo.saveAndFlush(quiz);
        correctOptionIndexes.forEach((question, index) ->
                question.setCorrectOptionId(question.getOptions().get(index).getId()));
        if (!correctOptionIndexes.isEmpty()) {
            savedQuiz = quizRepo.saveAndFlush(savedQuiz);
        }
        return mapQuiz(savedQuiz, teacher, true);
    }

    public QuizAttemptResponseDTO submitAttempt(
            Long quizId, QuizAttemptRequestDTO dto, Authentication authentication) {
        User learner = getRequester(authentication);
        Quiz quiz = findQuiz(quizId);
        requireEnrolledLearner(quiz, learner);
        OffsetDateTime now = OffsetDateTime.now();
        if (dto.isTimedOut() && now.isBefore(quiz.getClosesAt().minusSeconds(2))) {
            throw new IllegalArgumentException("The quiz timer has not expired");
        }
        if (now.isBefore(quiz.getOpensAt())
                || (now.isAfter(quiz.getClosesAt())
                && (!dto.isTimedOut() || now.isAfter(quiz.getClosesAt().plusMinutes(2))))) {
            throw new IllegalArgumentException("This quiz is not currently open");
        }
        if (attemptRepo.findByQuizIdAndLearnerId(quizId, learner.getId()).isPresent()) {
            throw new IllegalArgumentException("You have already submitted this quiz");
        }

        Set<Long> expectedQuestionIds = new HashSet<>();
        quiz.getQuestions().forEach(question -> expectedQuestionIds.add(question.getId()));
        Set<Long> answeredQuestionIds = new HashSet<>();
        Map<Long, QuizAnswerRequestDTO> answersByQuestion = new java.util.HashMap<>();
        for (QuizAnswerRequestDTO answer : dto.getAnswers()) {
            if (!expectedQuestionIds.contains(answer.getQuestionId())
                    || !answeredQuestionIds.add(answer.getQuestionId())) {
                throw new IllegalArgumentException("Quiz answers must contain each quiz question exactly once");
            }
            answersByQuestion.put(answer.getQuestionId(), answer);
        }
        if (!dto.isTimedOut() && !answeredQuestionIds.equals(expectedQuestionIds)) {
            throw new IllegalArgumentException("Please answer every question before submitting");
        }

        double autoGradedPoints = 0;
        boolean hasWrittenQuestions = false;
        for (QuizQuestion question : quiz.getQuestions()) {
            QuizAnswerRequestDTO answer = answersByQuestion.get(question.getId());
            if (answer == null) {
                if (!dto.isTimedOut()) {
                    throw new IllegalArgumentException("Please answer every question before submitting");
                }
                if (question.getQuestionType() == QuizQuestionType.WRITTEN) {
                    hasWrittenQuestions = true;
                }
                continue;
            }
            if (question.getQuestionType() == QuizQuestionType.MULTIPLE_CHOICE) {
                if ((!dto.isTimedOut() && answer.getSelectedOptionId() == null)
                        || hasText(answer.getResponseText())) {
                    throw new IllegalArgumentException("Select one option for each multiple-choice question");
                }
                if (answer.getSelectedOptionId() != null) {
                    boolean optionBelongsToQuestion = question.getOptions().stream()
                            .anyMatch(option -> option.getId().equals(answer.getSelectedOptionId()));
                    if (!optionBelongsToQuestion) {
                        throw new IllegalArgumentException("Selected option does not belong to this question");
                    }
                }
                if (answer.getSelectedOptionId() != null
                        && answer.getSelectedOptionId().equals(question.getCorrectOptionId())) {
                    autoGradedPoints += question.getMaxPoints();
                }
            } else {
                if (answer.getSelectedOptionId() != null
                        || !dto.isTimedOut() && !hasText(answer.getResponseText())) {
                    throw new IllegalArgumentException("Enter a written response for each written question");
                }
                hasWrittenQuestions = true;
            }
        }

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .learner(learner)
                .status(hasWrittenQuestions ? QuizAttemptStatus.SUBMITTED : QuizAttemptStatus.GRADED)
                .grade(autoGradedPoints)
                .gradedAt(hasWrittenQuestions ? null : LocalDateTime.now())
                .answers(new ArrayList<>())
                .build();
        for (QuizQuestion question : quiz.getQuestions()) {
            QuizAnswerRequestDTO answerDto = answersByQuestion.get(question.getId());
            attempt.getAnswers().add(QuizAnswer.builder()
                    .attempt(attempt)
                    .question(question)
                    .responseText(answerDto == null || answerDto.getResponseText() == null
                            ? "" : answerDto.getResponseText().trim())
                    .selectedOptionId(answerDto == null ? null : answerDto.getSelectedOptionId())
                    .build());
        }
        return mapAttempt(attemptRepo.saveAndFlush(attempt), false);
    }

    public List<QuizAttemptResponseDTO> getAttempts(Long quizId, Authentication authentication) {
        User requester = getRequester(authentication);
        Quiz quiz = findQuiz(quizId);
        requireTeacherOrAdmin(quiz, requester);
        return attemptRepo.findByQuizIdOrderBySubmittedAtDesc(quizId).stream()
                .map(attempt -> mapAttempt(attempt, true))
                .toList();
    }

    public QuizAttemptResponseDTO gradeAttempt(
            Long attemptId, QuizGradeRequestDTO dto, Authentication authentication) {
        User requester = getRequester(authentication);
        QuizAttempt attempt = attemptRepo.findById(attemptId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz attempt not found"));
        requireTeacherOrAdmin(attempt.getQuiz(), requester);
        if (attempt.getStatus() == QuizAttemptStatus.GRADED) {
            throw new IllegalArgumentException("This quiz attempt has already been graded");
        }
        double totalPoints = totalPoints(attempt.getQuiz());
        double autoGradedPoints = autoGradedPoints(attempt);
        if (dto.getGrade() < autoGradedPoints || dto.getGrade() > totalPoints) {
            throw new IllegalArgumentException(
                    "Final grade must be between " + autoGradedPoints + " and " + totalPoints);
        }
        attempt.setGrade(dto.getGrade());
        attempt.setFeedback(dto.getFeedback());
        attempt.setGradedAt(LocalDateTime.now());
        attempt.setStatus(QuizAttemptStatus.GRADED);
        return mapAttempt(attemptRepo.saveAndFlush(attempt), true);
    }

    private Quiz findQuiz(Long quizId) {
        return quizRepo.findById(quizId)
                .orElseThrow(() -> new EntityNotFoundException("Quiz not found"));
    }

    private void requireQuizAccess(Quiz quiz, User requester) {
        if (requester.getRole() == Role.ADMIN) return;
        if (requester.getRole() == Role.TEACHER) {
            requireTeacherOrAdmin(quiz, requester);
            return;
        }
        if (requester.getRole() == Role.LEARNER) {
            requireEnrolledLearner(quiz, requester);
            return;
        }
        throw new AccessDeniedException("You are not allowed to access this quiz");
    }

    private void requireEnrolledLearner(Quiz quiz, User learner) {
        if (learner.getRole() != Role.LEARNER
                || !enrollmentRepo.existsByCourseIdAndStudentId(quiz.getCourse().getId(), learner.getId())) {
            throw new AccessDeniedException("You can only access quizzes for courses you are enrolled in");
        }
    }

    private void requireTeacherOrAdmin(Quiz quiz, User requester) {
        if (requester.getRole() == Role.ADMIN) return;
        if (requester.getRole() != Role.TEACHER
                || quiz.getCourse().getTeacher() == null
                || !quiz.getCourse().getTeacher().getId().equals(requester.getId())) {
            throw new AccessDeniedException("You can only manage quizzes for your own courses");
        }
    }

    private User getRequester(Authentication authentication) {
        return userRepo.findByEmail(authentication.getName())
                .orElseThrow(() -> new EntityNotFoundException("Authenticated user was not found"));
    }

    private void validateQuestion(QuizQuestionRequestDTO dto) {
        if (dto.getQuestionType() == null) {
            throw new IllegalArgumentException("Choose a question type");
        }
        if (hasText(dto.getImageUrl())
                && !dto.getImageUrl().matches("^/api/quizzes/images/[A-Za-z0-9._-]+$")) {
            throw new IllegalArgumentException("Question images must be uploaded through the quiz image endpoint");
        }
        if (dto.getQuestionType() == QuizQuestionType.WRITTEN) {
            if (dto.getOptions() != null && !dto.getOptions().isEmpty()) {
                throw new IllegalArgumentException("Written questions cannot have answer options");
            }
            if (dto.getCorrectOptionIndex() != null) {
                throw new IllegalArgumentException("Written questions cannot have a correct option");
            }
            return;
        }
        if (dto.getOptions() == null || dto.getOptions().size() < 2) {
            throw new IllegalArgumentException("Multiple-choice questions need at least two options");
        }
        Set<String> normalizedOptions = new HashSet<>();
        for (String option : dto.getOptions()) {
            if (!hasText(option) || !normalizedOptions.add(option.trim().toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Multiple-choice options must be non-empty and unique");
            }
        }
        if (dto.getCorrectOptionIndex() == null
                || dto.getCorrectOptionIndex() < 0
                || dto.getCorrectOptionIndex() >= dto.getOptions().size()) {
            throw new IllegalArgumentException("Choose the correct multiple-choice option");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private QuizResponseDTO mapQuiz(Quiz quiz, User requester, boolean includeQuestions) {
        QuizAttempt myAttempt = requester.getRole() == Role.LEARNER
                ? attemptRepo.findByQuizIdAndLearnerId(quiz.getId(), requester.getId()).orElse(null)
                : null;
        return QuizResponseDTO.builder()
                .id(quiz.getId())
                .courseId(quiz.getCourse().getId())
                .courseName(quiz.getCourse().getName())
                .title(quiz.getTitle())
                .description(quiz.getDescription())
                .opensAt(quiz.getOpensAt())
                .closesAt(quiz.getClosesAt())
                .totalPoints(totalPoints(quiz))
                .hasMultipleChoiceQuestions(quiz.getQuestions().stream()
                        .anyMatch(question -> question.getQuestionType() == QuizQuestionType.MULTIPLE_CHOICE))
                .hasWrittenQuestions(quiz.getQuestions().stream()
                        .anyMatch(question -> question.getQuestionType() == QuizQuestionType.WRITTEN))
                .questions(includeQuestions ? quiz.getQuestions().stream().map(question -> QuizQuestionResponseDTO.builder()
                        .id(question.getId())
                        .prompt(question.getPrompt())
                        .maxPoints(question.getMaxPoints())
                        .questionType(question.getQuestionType())
                        .imageUrl(question.getImageUrl())
                        .options(question.getOptions().stream().map(option -> QuizOptionResponseDTO.builder()
                                .id(option.getId())
                                .text(option.getOptionText())
                                .build()).toList())
                        .correctOptionId(requester.getRole() == Role.LEARNER
                                && OffsetDateTime.now().isBefore(quiz.getClosesAt())
                                ? null : question.getCorrectOptionId())
                        .build()).toList() : List.of())
                .myAttemptId(myAttempt == null ? null : myAttempt.getId())
                .myAttemptStatus(myAttempt == null ? null : myAttempt.getStatus())
                .myGrade(myAttempt == null ? null : myAttempt.getGrade())
                .myAutoGradedPoints(myAttempt == null ? null : autoGradedPoints(myAttempt))
                .myFeedback(myAttempt == null ? null : myAttempt.getFeedback())
                .myAnswers(myAttempt == null ? List.of() : myAttempt.getAnswers().stream()
                        .map(answer -> QuizAnswerResponseDTO.builder()
                                .questionId(answer.getQuestion().getId())
                                .prompt(answer.getQuestion().getPrompt())
                                .maxPoints(answer.getQuestion().getMaxPoints())
                                .questionType(answer.getQuestion().getQuestionType())
                                .responseText(answer.getResponseText())
                                .selectedOptionId(answer.getSelectedOptionId())
                                .selectedOptionText(answer.getQuestion().getOptions().stream()
                                        .filter(option -> option.getId().equals(answer.getSelectedOptionId()))
                                        .map(QuizQuestionOption::getOptionText)
                                        .findFirst()
                                        .orElse(null))
                                .correctOptionId(OffsetDateTime.now().isBefore(quiz.getClosesAt())
                                        ? null : answer.getQuestion().getCorrectOptionId())
                                .correctOptionText(OffsetDateTime.now().isBefore(quiz.getClosesAt())
                                        ? null : answer.getQuestion().getOptions().stream()
                                                .filter(option -> option.getId().equals(answer.getQuestion()
                                                        .getCorrectOptionId()))
                                                .map(QuizQuestionOption::getOptionText)
                                                .findFirst()
                                                .orElse(null))
                                .build()).toList())
                .build();
    }

    private QuizAttemptResponseDTO mapAttempt(QuizAttempt attempt, boolean revealCorrectOptions) {
        return QuizAttemptResponseDTO.builder()
                .id(attempt.getId())
                .quizId(attempt.getQuiz().getId())
                .quizTitle(attempt.getQuiz().getTitle())
                .learnerId(attempt.getLearner().getId())
                .learnerName(attempt.getLearner().getFirstName() + " " + attempt.getLearner().getLastName())
                .submittedAt(attempt.getSubmittedAt())
                .gradedAt(attempt.getGradedAt())
                .grade(attempt.getGrade())
                .feedback(attempt.getFeedback())
                .autoGradedPoints(autoGradedPoints(attempt))
                .status(attempt.getStatus())
                .answers(attempt.getAnswers().stream().map(answer -> QuizAnswerResponseDTO.builder()
                        .questionId(answer.getQuestion().getId())
                        .prompt(answer.getQuestion().getPrompt())
                        .maxPoints(answer.getQuestion().getMaxPoints())
                        .questionType(answer.getQuestion().getQuestionType())
                        .responseText(answer.getResponseText())
                        .selectedOptionId(answer.getSelectedOptionId())
                        .selectedOptionText(answer.getQuestion().getOptions().stream()
                                .filter(option -> option.getId().equals(answer.getSelectedOptionId()))
                                .map(QuizQuestionOption::getOptionText)
                                .findFirst()
                                .orElse(null))
                        .correctOptionId(revealCorrectOptions
                                ? answer.getQuestion().getCorrectOptionId() : null)
                        .correctOptionText(revealCorrectOptions
                                ? answer.getQuestion().getOptions().stream()
                                        .filter(option -> option.getId().equals(answer.getQuestion().getCorrectOptionId()))
                                        .map(QuizQuestionOption::getOptionText)
                                        .findFirst()
                                        .orElse(null)
                                : null)
                        .build()).toList())
                .build();
    }

    private double autoGradedPoints(QuizAttempt attempt) {
        return attempt.getAnswers().stream()
                .filter(answer -> answer.getQuestion().getQuestionType() == QuizQuestionType.MULTIPLE_CHOICE)
                .filter(answer -> answer.getSelectedOptionId() != null
                        && answer.getSelectedOptionId().equals(answer.getQuestion().getCorrectOptionId()))
                .mapToDouble(answer -> answer.getQuestion().getMaxPoints())
                .sum();
    }

    private double totalPoints(Quiz quiz) {
        return quiz.getQuestions().stream().mapToDouble(QuizQuestion::getMaxPoints).sum();
    }
}
