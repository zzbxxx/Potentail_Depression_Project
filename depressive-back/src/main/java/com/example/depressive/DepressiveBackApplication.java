package com.example.depressive;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DepressiveBackApplication {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.configure().directory(".").load();
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
		SpringApplication.run(DepressiveBackApplication.class, args);
	}
}
