package Learning.Management.System.config;

import Learning.Management.System.entity.Announcement;
import Learning.Management.System.entity.Assignment;
import Learning.Management.System.entity.Attendance;
import Learning.Management.System.entity.AttendanceStatus;
import Learning.Management.System.entity.Course;
import Learning.Management.System.entity.Enrollment;
import Learning.Management.System.entity.Grade;
import Learning.Management.System.entity.Role;
import Learning.Management.System.entity.SchoolTerm;
import Learning.Management.System.entity.Submission;
import Learning.Management.System.entity.SubmissionStatus;
import Learning.Management.System.entity.User;
import Learning.Management.System.repository.AnnouncementRepo;
import Learning.Management.System.repository.AssignmentRepo;
import Learning.Management.System.repository.AttendanceRepo;
import Learning.Management.System.repository.CourseRepo;
import Learning.Management.System.repository.EnrollmentRepo;
import Learning.Management.System.repository.SubmissionRepo;
import Learning.Management.System.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SampleDataInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SampleDataInitializer.class);
    private static final int LEARNER_COUNT = 120;
    private static final int TEACHER_COUNT = 15;
    private static final int COURSES_PER_TEACHER = 2;
    private static final int ASSIGNMENTS_PER_COURSE = 12;
    private static final int COURSE_COUNT = TEACHER_COUNT * COURSES_PER_TEACHER;
    private static final String[] COURSE_NAMES = {
            "Mathematics", "Science", "English", "History", "Computer Studies",
            "Biology", "Chemistry", "Physics", "Geography", "Economics"
    };

    private final AssignmentRepo assignmentRepo;
    private final AnnouncementRepo announcementRepo;
    private final AttendanceRepo attendanceRepo;
    private final CourseRepo courseRepo;
    private final EnrollmentRepo enrollmentRepo;
    private final SubmissionRepo submissionRepo;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${lms.sample-data.password:}")
    private String samplePassword;

    @Value("${lms.sample-data.enabled:true}")
    private boolean sampleDataEnabled;

    @Override
    public void run(String... args) {
        if (!sampleDataEnabled) {
            LOGGER.info("Sample data generation is disabled. Set LMS_SAMPLE_DATA_ENABLED=true to enable it.");
            return;
        }
        if (samplePassword == null || samplePassword.isBlank()) {
            throw new IllegalStateException(
                    "Set LMS_SAMPLE_DATA_PASSWORD when LMS_SAMPLE_DATA_ENABLED is true");
        }

        List<User> teachers = createUsers(TEACHER_COUNT, Role.TEACHER, "teacher");
        List<User> learners = createUsers(LEARNER_COUNT, Role.LEARNER, "learner");
        List<Course> courses = createCourses(teachers);
        List<Assignment> assignments = createAssignments(courses);
        createEnrollments(learners, courses);
        createAttendance(learners, courses);
        createSubmissions(learners, courses, assignments);
        createAnnouncements(teachers, courses);

        LOGGER.info("Sample data ready: {} learners, {} teachers, {} courses, {} assignments, "
                        + "{} enrollments, {} attendance records, {} submissions and {} announcements",
                learners.size(), teachers.size(), courses.size(), assignments.size(),
                learners.size() * 3, learners.size() * 3, learners.size() * 3, teachers.size());
    }

    private List<User> createUsers(int count, Role role, String prefix) {
        List<User> users = new ArrayList<>(count);
        String encodedPassword = passwordEncoder.encode(samplePassword);

        for (int index = 1; index <= count; index++) {
            String email = String.format("%s%03d@example.test", prefix, index);
            String firstName = role == Role.TEACHER ? "Teacher" : "Learner";
            String lastName = String.format("%03d", index);
            User user = userRepo.findByEmail(email).orElseGet(() -> userRepo.save(User.builder()
                    .email(email)
                    .passwordHash(encodedPassword)
                    .firstName(firstName)
                    .lastName(lastName)
                    .role(role)
                    .build()));

            if (user.getRole() != role) {
                throw new IllegalStateException("Sample account email is already used by another role: " + email);
            }
            users.add(user);
        }
        return users;
    }

    private List<Course> createCourses(List<User> teachers) {
        List<Course> courses = new ArrayList<>(COURSE_COUNT);
        for (int index = 0; index < COURSE_COUNT; index++) {
            int courseNumber = index + 1;
            String code = String.format("SAMPLE-%03d", courseNumber);
            String name = COURSE_NAMES[index % COURSE_NAMES.length] + " " + courseNumber;
            SchoolTerm schoolTerm = SchoolTerm.values()[index % SchoolTerm.values().length];
            User teacher = teachers.get(index / COURSES_PER_TEACHER);
            Course course = courseRepo.findByCode(code).orElseGet(() -> courseRepo.save(Course.builder()
                    .name(name)
                    .code(code)
                    .description("Sample course for LMS pagination and workflow testing.")
                    .term("2026-2027")
                    .schoolTerm(schoolTerm)
                    .teacher(teacher)
                    .build()));
            courses.add(course);
        }
        return courses;
    }

    private List<Assignment> createAssignments(List<Course> courses) {
        List<Assignment> assignments = new ArrayList<>(courses.size() * ASSIGNMENTS_PER_COURSE);
        for (Course course : courses) {
            for (int assignmentNumber = 1; assignmentNumber <= ASSIGNMENTS_PER_COURSE; assignmentNumber++) {
                String title = String.format("Sample assignment %02d - %s", assignmentNumber, course.getCode());
                Assignment assignment = assignmentRepo.findByCourseIdAndTitle(course.getId(), title)
                        .orElseGet(() -> assignmentRepo.save(Assignment.builder()
                                .course(course)
                                .title(title)
                                .description("A sample assignment generated for demonstration.")
                                .dueDate(LocalDateTime.now().plusDays(14))
                                .maxScore(100.0)
                                .build()));
                assignments.add(assignment);
            }
        }
        return assignments;
    }

    private void createEnrollments(List<User> learners, List<Course> courses) {
        for (int learnerIndex = 0; learnerIndex < learners.size(); learnerIndex++) {
            User learner = learners.get(learnerIndex);
            for (int courseOffset = 0; courseOffset < 3; courseOffset++) {
                Course course = courses.get((learnerIndex * 3 + courseOffset) % courses.size());
                if (!enrollmentRepo.existsByCourseIdAndStudentId(course.getId(), learner.getId())) {
                    enrollmentRepo.save(Enrollment.builder()
                            .course(course)
                            .student(learner)
                            .grade(Grade.values()[learnerIndex % Grade.values().length])
                            .build());
                }
            }
        }
    }

    private void createAttendance(List<User> learners, List<Course> courses) {
        LocalDate attendanceDate = LocalDate.of(2026, 1, 5);
        for (int learnerIndex = 0; learnerIndex < learners.size(); learnerIndex++) {
            User learner = learners.get(learnerIndex);
            for (int courseOffset = 0; courseOffset < 3; courseOffset++) {
                Course course = courses.get((learnerIndex * 3 + courseOffset) % courses.size());
                LocalDate date = attendanceDate.plusDays(courseOffset);
                if (!attendanceRepo.existsByCourseIdAndStudentIdAndDate(course.getId(), learner.getId(), date)) {
                    attendanceRepo.save(Attendance.builder()
                            .course(course)
                            .student(learner)
                            .date(date)
                            .status(AttendanceStatus.values()[learnerIndex % AttendanceStatus.values().length])
                            .build());
                }
            }
        }
    }

    private void createSubmissions(List<User> learners, List<Course> courses, List<Assignment> assignments) {
        for (int learnerIndex = 0; learnerIndex < learners.size(); learnerIndex++) {
            User learner = learners.get(learnerIndex);
            for (int courseOffset = 0; courseOffset < 3; courseOffset++) {
                int courseIndex = (learnerIndex * 3 + courseOffset) % courses.size();
                Assignment assignment = assignments.get(courseIndex * ASSIGNMENTS_PER_COURSE);
                if (!submissionRepo.existsByStudentIdAndAssignmentId(learner.getId(), assignment.getId())) {
                    submissionRepo.save(Submission.builder()
                            .assignment(assignment)
                            .student(learner)
                            .grade(75.0 + learnerIndex % 26)
                            .feedback("Sample feedback.")
                            .status(SubmissionStatus.GRADED)
                            .build());
                }
            }
        }
    }

    private void createAnnouncements(List<User> teachers, List<Course> courses) {
        for (int index = 0; index < teachers.size(); index++) {
            User teacher = teachers.get(index);
            String title = String.format("Sample announcement - teacher%03d", index + 1);
            if (!announcementRepo.existsByAuthorIdAndTitle(teacher.getId(), title)) {
                announcementRepo.save(Announcement.builder()
                        .title(title)
                        .content("This is a sample announcement for LMS demonstration.")
                        .author(teacher)
                        .course(courses.get(index * COURSES_PER_TEACHER))
                        .build());
            }
        }
    }
}
