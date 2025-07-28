package com.susang.stellarVision;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class StellarVisionApplication {

	public static void main(String[] args) {
		SpringApplication.run(StellarVisionApplication.class, args);
	}

}
