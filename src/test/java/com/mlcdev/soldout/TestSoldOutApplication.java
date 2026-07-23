package com.mlcdev.soldout;

import org.springframework.boot.SpringApplication;

public class TestSoldOutApplication {

	public static void main(String[] args) {
		SpringApplication.from(SoldOutApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
