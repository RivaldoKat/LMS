package Learning.Management.System;

import Learning.Management.System.repository.StorageRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(SystemApplication.class, args);
	}

	@Bean
	CommandLineRunner init(StorageRepo storageRepo) {
		return (args) -> {
			storageRepo.init();
		};
	}

}
