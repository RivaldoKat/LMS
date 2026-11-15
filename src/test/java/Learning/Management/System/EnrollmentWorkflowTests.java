package Learning.Management.System;

import Learning.Management.System.entity.Course;
import Learning.Management.System.entity.Role;
import Learning.Management.System.entity.SchoolTerm;
import Learning.Management.System.entity.User;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:enrollment-workflow;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "jwt.secret=test-only-signing-key-that-is-at-least-thirty-two-bytes",
        "lms.sample-data.enabled=false"
})
@AutoConfigureMockMvc
@Transactional
class EnrollmentWorkflowTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CourseRepo courseRepo;

    private User learner;
    private Course course;

    @BeforeEach
    void setUp() {
        User teacher = saveUser("teacher-enrollment@example.test", Role.TEACHER);
        learner = saveUser("learner-enrollment@example.test", Role.LEARNER);
        course = courseRepo.save(Course.builder()
                .name("Enrollment course")
                .code("ENROLL-101")
                .term("Test term")
                .schoolTerm(SchoolTerm.Term1)
                .teacher(teacher)
                .build());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void staffCanLoadLearnersAndEnrollWithGrade() throws Exception {
        mockMvc.perform(get("/api/users/learners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(learner.getId()));

        mockMvc.perform(post("/api/enrollments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":" + course.getId()
                                + ",\"studentId\":" + learner.getId()
                                + ",\"grade\":\"Eight\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grade").value("Eight"))
                .andExpect(jsonPath("$.studentId").value(learner.getId()));
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void teachersCanLoadLearnerOptionsForEnrollment() throws Exception {
        mockMvc.perform(get("/api/users/learners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(learner.getId()))
                .andExpect(jsonPath("$.content[0].email").value("learner-enrollment@example.test"));
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
