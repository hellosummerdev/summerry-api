package com.summerry;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan(basePackages = "com.summerry.user.mapper")
public class SummerryApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SummerryApiApplication.class, args);
	}

}
