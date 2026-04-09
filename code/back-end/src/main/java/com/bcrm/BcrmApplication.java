package com.bcrm;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * BCRM 美容院客户关系管理系统 - 启动类
 *
 * @author BCRM
 * @since 2026-03-14
 */
@SpringBootApplication
@EnableScheduling
@MapperScan("com.bcrm.mapper")
public class BcrmApplication {

    public static void main(String[] args) {
        SpringApplication.run(BcrmApplication.class, args);
        System.out.println("==========================================");
        System.out.println("       BCRM 系统启动成功！");
        System.out.println("       API文档地址: http://localhost:8080/api/doc.html");
        System.out.println("==========================================");
    }
}
