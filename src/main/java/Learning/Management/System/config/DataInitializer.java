package Learning.Management.System.config;

import Learning.Management.System.model.Course;
import Learning.Management.System.model.Enrollment;
import Learning.Management.System.model.Role;
import Learning.Management.System.model.User;
import Learning.Management.System.repository.CourseRepo;
import Learning.Management.System.repository.EnrollmentRepo;
import Learning.Management.System.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepo userRepo;
    private final CourseRepo courseRepo;
    private final EnrollmentRepo enrollmentRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if ( userRepo.count() == 0) {
            User teacher = User.builder()
                    .email("teacher@school.edu")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .firstName("Kaltego")
                    .lastName("Sekome")
                    .role(Role.TEACHER)
                    .build();
            userRepo.save(teacher);

            User learner = User.builder()
                    .email("learner@school.edu")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .firstName("Rivaldo")
                    .lastName("Sekome")
                    .role(Role.LEARNER)
                    .build();
            userRepo.save(learner);

            Course course = Course.builder()
                    .name("Algebra")
                    .code("MATH-101")
                    .description("Introductory algebra for high schoolers.")
                    .term("Term 1")
                    .teacher(teacher)
                    .build();
            courseRepo.save(course);

            Enrollment enrollment = Enrollment.builder()
                    .course(course)
                    .student(learner)
                    .build();
            enrollmentRepo.save(enrollment);

            System.out.println(" [DataInitializer] Seed data successfully loaded into H2 Database");
        }
    }
}
