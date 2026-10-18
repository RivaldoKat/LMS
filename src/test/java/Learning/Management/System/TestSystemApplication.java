package Learning.Management.System;

import org.springframework.boot.SpringApplication;

public class TestSystemApplication {

	public static void main(String[] args) {
		SpringApplication.from(SystemApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
