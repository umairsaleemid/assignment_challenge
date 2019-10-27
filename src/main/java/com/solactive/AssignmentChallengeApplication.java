package com.solactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main APP, entry point
 * 
 * @author
 *
 */

@SpringBootApplication
@EnableScheduling
public class AssignmentChallengeApplication {

	public static void main(String[] args) {
		SpringApplication.run(AssignmentChallengeApplication.class, args);

	}

}
