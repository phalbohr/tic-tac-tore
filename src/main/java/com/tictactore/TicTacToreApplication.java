package com.tictactore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@org.springframework.scheduling.annotation.EnableAsync
public class TicTacToreApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicTacToreApplication.class, args);
	}

}
