package Learning.Management.System;

import Learning.Management.System.entity.Assignment;
import Learning.Management.System.entity.Course;
import Learning.Management.System.entity.Role;
import Learning.Management.System.entity.SchoolTerm;
import Learning.Management.System.entity.Submission;
import Learning.Management.System.entity.SubmissionStatus;
import Learning.Management.System.entity.User;
import Learning.Management.System.repository.AssignmentRepo;
import Learning.Management.System.repository.CourseRepo;
import Learning.Management.System.repository.StorageRepo;
import Learning.Management.System.repository.SubmissionRepo;
import Learning.Management.System.repository.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:submission-authorization;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
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

    @Autowired
    private SubmissionRepo submissionRepo;

    @Autowired
    private StorageRepo storageRepo;

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
                .schoolTerm(SchoolTerm.Term1)
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
    @WithMockUser(username = "learner@example.test", roles = "LEARNER")
    void learnerCanReadRecentGradesForTheirOwnSubmissions() throws Exception {
        submissionRepo.save(Submission.builder()
                .assignment(assignment)
                .student(learner)
                .grade(92.0)
                .gradedAt(LocalDateTime.now())
                .status(SubmissionStatus.GRADED)
                .build());

        mockMvc.perform(get("/api/submissions/recent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].assignmentTitle").value("Test assignment"))
                .andExpect(jsonPath("$.content[0].grade").value(92.0))
                .andExpect(jsonPath("$.content[0].gradedAt").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "teacher@example.test", roles = "TEACHER")
    void teacherCanListSubmissionsForTheirAssignmentAndDownloadAttachment() throws Exception {
        String storedFilename = storageRepo.store(new MockMultipartFile(
                "file", "work.pdf", MediaType.APPLICATION_PDF_VALUE,
                "learner work".getBytes(StandardCharsets.UTF_8)));
        try {
            Submission submission = submissionRepo.saveAndFlush(Submission.builder()
                    .assignment(assignment)
                    .student(learner)
                    .fileUrl("/files/" + storedFilename)
                    .status(SubmissionStatus.SUBMITTED)
                    .build());

            mockMvc.perform(get("/api/submissions/assignment/{assignmentId}", assignment.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].fileUrl")
                            .value("/api/submissions/" + submission.getId() + "/attachment"));

            mockMvc.perform(get("/api/submissions/{id}/attachment", submission.getId()))
                    .andExpect(status().isOk())
                    .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                            .bytes("learner work".getBytes(StandardCharsets.UTF_8)));
        } finally {
            storageRepo.deleteFile(storedFilename);
        }
    }

    @Test
    @WithMockUser(username = "other-learner@example.test", roles = "LEARNER")
    void learnerCannotDownloadAnotherStudentsAttachment() throws Exception {
        String storedFilename = storageRepo.store(new MockMultipartFile(
                "file", "work.pdf", MediaType.APPLICATION_PDF_VALUE,
                "learner work".getBytes(StandardCharsets.UTF_8)));
        try {
            Submission submission = submissionRepo.saveAndFlush(Submission.builder()
                    .assignment(assignment)
                    .student(learner)
                    .fileUrl("/files/" + storedFilename)
                    .status(SubmissionStatus.SUBMITTED)
                    .build());

            mockMvc.perform(get("/api/submissions/{id}/attachment", submission.getId()))
                    .andExpect(status().isForbidden());
        } finally {
            storageRepo.deleteFile(storedFilename);
        }
    }

    @Test
    @WithMockUser(username = "teacher@example.test", roles = "TEACHER")
    void teacherCannotUseAdminOnlyCourseCreation() throws Exception {
        mockMvc.perform(post("/api/courses")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"New course\",\"code\":\"NEW-101\","
                                        + "\"term\":\"Test term\",\"schoolTerm\":\"Term1\",\"teacherId\":1}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "teacher@example.test", roles = "TEACHER")
    void teacherCannotMoveOrDeleteAnotherTeachersAssignment() throws Exception {
        User otherTeacher = saveUser("other-teacher@example.test", Role.TEACHER);
        Course otherCourse = courseRepo.save(Course.builder()
                .name("Other course")
                .code("OTHER-101")
                .term("Test term")
                .schoolTerm(SchoolTerm.Term1)
                .teacher(otherTeacher)
                .build());
        Assignment otherAssignment = assignmentRepo.save(Assignment.builder()
                .course(otherCourse)
                .title("Other assignment")
                .description("Test")
                .dueDate(LocalDateTime.now().plusDays(1))
                .maxScore(100.0)
                .build());

        mockMvc.perform(put("/api/assignments/{id}", assignment.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"courseId\":" + otherCourse.getId()
                                + ",\"title\":\"Moved\",\"description\":\"Test\","
                                + "\"dueDate\":\"2030-01-01T00:00:00\",\"maxScore\":100}"))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/api/assignments/{id}", otherAssignment.getId()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/submissions/assignment/{assignmentId}", otherAssignment.getId()))
                .andExpect(status().isForbidden());
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
