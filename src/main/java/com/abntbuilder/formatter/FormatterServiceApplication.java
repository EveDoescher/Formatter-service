package com.abntbuilder.formatter;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FormatterServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FormatterServiceApplication.class, args);
	}

}
