package com.mmc.bookduck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class BookduckApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookduckApplication.class, args);
	}

}
