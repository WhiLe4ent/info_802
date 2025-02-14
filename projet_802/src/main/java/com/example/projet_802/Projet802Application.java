package com.example.projet_802;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.ws.config.annotation.EnableWs;

import io.github.cdimascio.dotenv.Dotenv;

@EnableWs
@SpringBootApplication
@ComponentScan(basePackages = "com.example")
@EnableCaching
public class Projet802Application {

	public static void main(String[] args) {
		Dotenv dotenv = Dotenv.load();
		dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));	

		SpringApplication.run(Projet802Application.class, args);
	}

}
