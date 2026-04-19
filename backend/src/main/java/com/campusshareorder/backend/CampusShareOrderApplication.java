package com.campusshareorder.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.campusshareorder.backend.mapper")
public class CampusShareOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusShareOrderApplication.class, args);
    }

}