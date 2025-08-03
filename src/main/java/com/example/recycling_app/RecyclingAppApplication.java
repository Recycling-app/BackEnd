package com.example.recycling_app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RecyclingAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(RecyclingAppApplication.class, args);
	}
}
