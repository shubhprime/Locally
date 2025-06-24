package com.locally.locally_backend_engine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableCaching
@EnableFeignClients(basePackages = "com.locally.locally_backend_engine.client")
@SpringBootApplication
public class LocallyBackendEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(LocallyBackendEngineApplication.class, args);
	}

}