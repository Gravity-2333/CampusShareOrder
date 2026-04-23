package com.campusshareorder.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.campusshareorder.backend.mapper")
@EnableScheduling  // 启用定时任务
public class CampusShareOrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusShareOrderApplication.class, args);
    }

}