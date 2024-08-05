package de.clemens.stream;

import de.clemens.stream.service.FilesStorageService;
import jakarta.annotation.Resource;

import java.sql.SQLException;

import org.h2.tools.Server;
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

    @Bean(initMethod = "start", destroyMethod = "stop")
    public Server h2Server() throws SQLException {
        return Server.createTcpServer("-tcp", "-tcpAllowOthers", "-tcpPort", "9092");
    }
}
