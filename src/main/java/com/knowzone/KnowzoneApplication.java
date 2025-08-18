package com.knowzone;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class KnowzoneApplication {

	public static void main(String[] args) {
		SpringApplication.run(KnowzoneApplication.class, args);
	}

}
