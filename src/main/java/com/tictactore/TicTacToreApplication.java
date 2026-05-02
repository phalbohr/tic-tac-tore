package com.tictactore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TicTacToreApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicTacToreApplication.class, args);
	}

}
