package Learning.Management.System;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:system-application;DB_CLOSE_DELAY=-1",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"jwt.secret=test-only-signing-key-that-is-at-least-thirty-two-bytes",
		"lms.bootstrap-admin.email=test-admin@example.test",
		"lms.bootstrap-admin.password=test-only-admin-password"
})
class SystemApplicationTests {

	@Test
	void contextLoads() {
	}

}
