package com.bobo.eleme;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.bobo.eleme.dao")
public class ElemeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ElemeApplication.class, args);
	}
}
