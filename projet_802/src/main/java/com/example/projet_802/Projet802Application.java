package com.example.projet_802;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.ws.config.annotation.EnableWs;

@EnableWs
@SpringBootApplication
@ComponentScan(basePackages = "com.example")
public class Projet802Application {

	public static void main(String[] args) {
		SpringApplication.run(Projet802Application.class, args);
	}

}
