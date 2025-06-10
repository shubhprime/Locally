package com.locally.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableCaching
@EnableFeignClients(basePackages = "com.locally.backend.client")
@SpringBootApplication
public class LocallyApplication {

	public static void main(String[] args) {
		SpringApplication.run(LocallyApplication.class, args);
	}

}