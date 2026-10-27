package Learning.Management.System.config;

import Learning.Management.System.model.Role;
import Learning.Management.System.model.User;
import Learning.Management.System.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${lms.bootstrap-admin.email:}")
    private String bootstrapAdminEmail;

    @Value("${lms.bootstrap-admin.password:}")
    private String bootstrapAdminPassword;

    @Override
    public void run(String... args) {
        if (!userRepo.existsByRole(Role.ADMIN)) {
            boolean hasEmail = bootstrapAdminEmail != null && !bootstrapAdminEmail.isBlank();
            boolean hasPassword = bootstrapAdminPassword != null && !bootstrapAdminPassword.isBlank();

            if (hasEmail != hasPassword) {
                throw new IllegalStateException(
                        "Set both LMS_ADMIN_EMAIL and LMS_ADMIN_PASSWORD to bootstrap the admin account");
            }

            if (hasEmail) {
                if (userRepo.findByEmail(bootstrapAdminEmail).isPresent()) {
                    throw new IllegalStateException(
                            "LMS_ADMIN_EMAIL is already used by a non-admin account");
                }
                User admin = User.builder()
                        .email(bootstrapAdminEmail)
                        .passwordHash(passwordEncoder.encode(bootstrapAdminPassword))
                        .firstName("System")
                        .lastName("Administrator")
                        .role(Role.ADMIN)
                        .build();
                userRepo.save(admin);
                LOGGER.info("Bootstrapped the initial admin account: {}", bootstrapAdminEmail);
            } else {
                LOGGER.warn("No ADMIN user exists. Set LMS_ADMIN_EMAIL and LMS_ADMIN_PASSWORD to bootstrap one.");
            }
        }
    }
}
