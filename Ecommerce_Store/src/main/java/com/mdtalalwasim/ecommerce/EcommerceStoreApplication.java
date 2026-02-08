package com.mdtalalwasim.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import com.mdtalalwasim.ecommerce.config.MailProperties;

@SpringBootApplication
@EnableConfigurationProperties(MailProperties.class)
public class EcommerceStoreApplication {

	public static void main(String[] args) {
		SpringApplication.run(EcommerceStoreApplication.class, args);
		System.out.println("Welcome");
	}

}
