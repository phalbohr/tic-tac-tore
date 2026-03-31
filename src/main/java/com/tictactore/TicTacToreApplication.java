package com.tictactore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class TicTacToreApplication {

	public static void main(String[] args) {
		SpringApplication.run(TicTacToreApplication.class, args);
	}

}
