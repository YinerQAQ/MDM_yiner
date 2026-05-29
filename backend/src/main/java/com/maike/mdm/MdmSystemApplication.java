package com.maike.mdm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.maike.mdm.mapper")
@EnableScheduling
public class MdmSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(MdmSystemApplication.class, args);
    }
}