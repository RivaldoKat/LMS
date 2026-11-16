package Learning.Management.System;

import Learning.Management.System.entity.Course;
import Learning.Management.System.entity.Enrollment;
import Learning.Management.System.entity.Grade;
import Learning.Management.System.entity.Quiz;
import Learning.Management.System.entity.QuizAttemptStatus;
import Learning.Management.System.entity.QuizQuestion;
import Learning.Management.System.entity.QuizQuestionType;
import Learning.Management.System.entity.Role;
import Learning.Management.System.entity.SchoolTerm;
import Learning.Management.System.entity.User;
import Learning.Management.System.repository.CourseRepo;
import Learning.Management.System.repository.EnrollmentRepo;
import Learning.Management.System.repository.QuizAttemptRepo;
import Learning.Management.System.repository.QuizRepo;
import Learning.Management.System.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:quiz-submission;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "jwt.secret=test-only-signing-key-that-is-at-least-thirty-two-bytes",
        "lms.sample-data.enabled=false"
})
@AutoConfigureMockMvc
@Transactional
class QuizSubmissionTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private EnrollmentRepo enrollmentRepo;

    @Autowired
    private QuizRepo quizRepo;

    @Autowired
    private QuizAttemptRepo attemptRepo;

    private User learner;
    private Quiz quiz;

    @BeforeEach
    void setUp() {
        User teacher = saveUser("quiz-teacher@example.test", Role.TEACHER);
        learner = saveUser("quiz-learner@example.test", Role.LEARNER);
        Course course = courseRepo.save(Course.builder()
                .name("Quiz course")
                .code("QUIZ-101")
                .term("Test term")
                .schoolTerm(SchoolTerm.Term1)
                .teacher(teacher)
                .build());
        enrollmentRepo.save(Enrollment.builder()
                .course(course)
                .student(learner)
                .grade(Grade.Eight)
                .build());

        QuizQuestion question = QuizQuestion.builder()
                .prompt("Write a response")
                .maxPoints(5.0)
                .questionType(QuizQuestionType.WRITTEN)
                .options(new ArrayList<>())
                .build();
        quiz = Quiz.builder()
                .course(course)
                .title("Open quiz")
                .opensAt(OffsetDateTime.now().minusMinutes(1))
                .closesAt(OffsetDateTime.now().plusMinutes(30))
                .questions(new ArrayList<>())
                .build();
        question.setQuiz(quiz);
        quiz.getQuestions().add(question);
        quiz = quizRepo.saveAndFlush(quiz);
    }

    @Test
    @WithMockUser(username = "quiz-learner@example.test", roles = "LEARNER")
    void submitsQuizWhenNoAnswersFieldWasSent() throws Exception {
        mockMvc.perform(post("/api/quizzes/{quizId}/attempts", quiz.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(QuizAttemptStatus.SUBMITTED.name()))
                .andExpect(jsonPath("$.answers[0].responseText").value(""));

        org.junit.jupiter.api.Assertions.assertTrue(
                attemptRepo.findByQuizIdAndLearnerId(quiz.getId(), learner.getId()).isPresent());
    }

    private User saveUser(String email, Role role) {
        return userRepo.save(User.builder()
                .email(email)
                .passwordHash("test-password-hash")
                .firstName("Test")
                .lastName("User")
                .role(role)
                .build());
    }
}
