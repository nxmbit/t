package com.pawelholownia.payback_zadanie;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class PaybackZadanieApplication {
	public static void main(String[] args) {
		SpringApplication.run(PaybackZadanieApplication.class, args);
	}
}