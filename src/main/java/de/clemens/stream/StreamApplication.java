package de.clemens.stream;

import de.clemens.stream.service.FilesStorageService;
import jakarta.annotation.Resource;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

@SpringBootApplication
public class StreamApplication {
    @Resource
    FilesStorageService storageService;
	public static void main(String[] args) {
		SpringApplication.run(StreamApplication.class, args);
	}

	@Bean
    public ApplicationRunner applicationRunner(Environment environment) {
        return args -> {
            //System.out.println("Konfiguration: " + environment.getProperty("spring.datasource.username"));
            storageService.init();
        };
    }
}
