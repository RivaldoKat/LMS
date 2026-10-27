package Learning.Management.System;

import Learning.Management.System.model.Assignment;
import Learning.Management.System.model.Course;
import Learning.Management.System.model.Role;
import Learning.Management.System.model.User;
import Learning.Management.System.repository.AssignmentRepo;
import Learning.Management.System.repository.CourseRepo;
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

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "jwt.secret=test-only-signing-key-that-is-at-least-thirty-two-bytes",
        "lms.bootstrap-admin.email=test-admin@example.test",
        "lms.bootstrap-admin.password=test-only-admin-password"
})
@AutoConfigureMockMvc
@Transactional
class SubmissionAuthorizationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CourseRepo courseRepo;

    @Autowired
    private AssignmentRepo assignmentRepo;

    private User learner;
    private User otherLearner;
    private Assignment assignment;

    @BeforeEach
    void setUp() {
        User teacher = saveUser("teacher@example.test", Role.TEACHER);
        learner = saveUser("learner@example.test", Role.LEARNER);
        otherLearner = saveUser("other-learner@example.test", Role.LEARNER);

        Course course = courseRepo.save(Course.builder()
                .name("Test course")
                .code("TEST-101")
                .term("Test term")
                .teacher(teacher)
                .build());

        assignment = assignmentRepo.save(Assignment.builder()
                .course(course)
                .title("Test assignment")
                .description("Test")
                .dueDate(LocalDateTime.now().plusDays(1))
                .maxScore(100.0)
                .build());
    }

    @Test
    @WithMockUser(username = "learner@example.test", roles = "LEARNER")
    void learnerCannotSubmitOnBehalfOfAnotherStudent() throws Exception {
        mockMvc.perform(post("/api/submissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"assignmentId\":" + assignment.getId()
                                + ",\"studentId\":" + otherLearner.getId()
                                + ",\"fileUrl\":\"upload.pdf\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentId").value(learner.getId()));
    }

    @Test
    @WithMockUser(username = "learner@example.test", roles = "LEARNER")
    void learnerCannotReadAnotherStudentsSubmissions() throws Exception {
        mockMvc.perform(get("/api/submissions/student/{studentId}", otherLearner.getId()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "teacher@example.test", roles = "TEACHER")
    void teacherCannotUseAdminOnlyCourseCreation() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New course\",\"code\":\"NEW-101\","
                                + "\"term\":\"Test term\",\"teacherId\":1}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
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
