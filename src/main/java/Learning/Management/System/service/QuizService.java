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
import java.util.List;
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
        boolean includeQuestions = requester.getRole() != Role.LEARNER
                || (!OffsetDateTime.now().isBefore(quiz.getOpensAt())
                && !OffsetDateTime.now().isAfter(quiz.getClosesAt()));
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
        for (QuizQuestionRequestDTO questionDto : dto.getQuestions()) {
            quiz.getQuestions().add(QuizQuestion.builder()
                    .quiz(quiz)
                    .prompt(questionDto.getPrompt().trim())
                    .maxPoints(questionDto.getMaxPoints())
                    .build());
        }
        return mapQuiz(quizRepo.save(quiz), teacher, true);
    }

    public QuizAttemptResponseDTO submitAttempt(
            Long quizId, QuizAttemptRequestDTO dto, Authentication authentication) {
        User learner = getRequester(authentication);
        Quiz quiz = findQuiz(quizId);
        requireEnrolledLearner(quiz, learner);
        OffsetDateTime now = OffsetDateTime.now();
        if (now.isBefore(quiz.getOpensAt()) || now.isAfter(quiz.getClosesAt())) {
            throw new IllegalArgumentException("This quiz is not currently open");
        }
        if (attemptRepo.findByQuizIdAndLearnerId(quizId, learner.getId()).isPresent()) {
            throw new IllegalArgumentException("You have already submitted this quiz");
        }

        Set<Long> expectedQuestionIds = new HashSet<>();
        quiz.getQuestions().forEach(question -> expectedQuestionIds.add(question.getId()));
        Set<Long> answeredQuestionIds = new HashSet<>();
        for (QuizAnswerRequestDTO answer : dto.getAnswers()) {
            if (!expectedQuestionIds.contains(answer.getQuestionId())
                    || !answeredQuestionIds.add(answer.getQuestionId())) {
                throw new IllegalArgumentException("Quiz answers must contain each quiz question exactly once");
            }
        }
        if (!answeredQuestionIds.equals(expectedQuestionIds)) {
            throw new IllegalArgumentException("Please answer every question before submitting");
        }

        QuizAttempt attempt = QuizAttempt.builder()
                .quiz(quiz)
                .learner(learner)
                .status(QuizAttemptStatus.SUBMITTED)
                .answers(new ArrayList<>())
                .build();
        for (QuizAnswerRequestDTO answerDto : dto.getAnswers()) {
            QuizQuestion question = quiz.getQuestions().stream()
                    .filter(candidate -> candidate.getId().equals(answerDto.getQuestionId()))
                    .findFirst()
                    .orElseThrow();
            attempt.getAnswers().add(QuizAnswer.builder()
                    .attempt(attempt)
                    .question(question)
                    .responseText(answerDto.getResponseText().trim())
                    .build());
        }
        return mapAttempt(attemptRepo.save(attempt));
    }

    public List<QuizAttemptResponseDTO> getAttempts(Long quizId, Authentication authentication) {
        User requester = getRequester(authentication);
        Quiz quiz = findQuiz(quizId);
        requireTeacherOrAdmin(quiz, requester);
        return attemptRepo.findByQuizIdOrderBySubmittedAtDesc(quizId).stream()
                .map(this::mapAttempt)
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
        if (dto.getGrade() > totalPoints) {
            throw new IllegalArgumentException("Grade cannot exceed the quiz total of " + totalPoints);
        }
        attempt.setGrade(dto.getGrade());
        attempt.setFeedback(dto.getFeedback());
        attempt.setGradedAt(LocalDateTime.now());
        attempt.setStatus(QuizAttemptStatus.GRADED);
        return mapAttempt(attemptRepo.save(attempt));
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
                .questions(includeQuestions ? quiz.getQuestions().stream().map(question -> QuizQuestionResponseDTO.builder()
                        .id(question.getId())
                        .prompt(question.getPrompt())
                        .maxPoints(question.getMaxPoints())
                        .build()).toList() : List.of())
                .myAttemptId(myAttempt == null ? null : myAttempt.getId())
                .myAttemptStatus(myAttempt == null ? null : myAttempt.getStatus())
                .myGrade(myAttempt == null ? null : myAttempt.getGrade())
                .myFeedback(myAttempt == null ? null : myAttempt.getFeedback())
                .build();
    }

    private QuizAttemptResponseDTO mapAttempt(QuizAttempt attempt) {
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
                .status(attempt.getStatus())
                .answers(attempt.getAnswers().stream().map(answer -> QuizAnswerResponseDTO.builder()
                        .questionId(answer.getQuestion().getId())
                        .prompt(answer.getQuestion().getPrompt())
                        .maxPoints(answer.getQuestion().getMaxPoints())
                        .responseText(answer.getResponseText())
                        .build()).toList())
                .build();
    }

    private double totalPoints(Quiz quiz) {
        return quiz.getQuestions().stream().mapToDouble(QuizQuestion::getMaxPoints).sum();
    }
}
