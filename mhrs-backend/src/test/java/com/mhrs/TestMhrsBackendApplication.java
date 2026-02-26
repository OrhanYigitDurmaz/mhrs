package com.mhrs;

import org.springframework.boot.SpringApplication;

public class TestMhrsBackendApplication {

	public static void main(String[] args) {
		SpringApplication.from(MhrsBackendApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
