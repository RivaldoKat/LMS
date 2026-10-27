package Learning.Management.System;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest(properties = {
		"jwt.secret=test-only-signing-key-that-is-at-least-thirty-two-bytes",
		"lms.bootstrap-admin.email=test-admin@example.test",
		"lms.bootstrap-admin.password=test-only-admin-password"
})
class SystemApplicationTests {

	@Test
	void contextLoads() {
	}

}
