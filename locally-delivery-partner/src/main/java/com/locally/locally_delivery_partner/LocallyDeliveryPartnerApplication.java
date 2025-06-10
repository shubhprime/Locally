package com.locally.locally_delivery_partner;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableCaching
@EnableFeignClients(basePackages = "com.locally.backend.client")
@SpringBootApplication
public class LocallyDeliveryPartnerApplication {

	public static void main(String[] args) {
		SpringApplication.run(LocallyDeliveryPartnerApplication.class, args);
	}

}