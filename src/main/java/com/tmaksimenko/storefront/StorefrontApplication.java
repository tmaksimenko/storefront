package com.tmaksimenko.storefront;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class StorefrontApplication {


	public static void main(String[] args) {

		SpringApplication.run(StorefrontApplication.class, args);

		log.info("App Started!");
	}

}
