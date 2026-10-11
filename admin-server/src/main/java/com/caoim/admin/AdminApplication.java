package com.caoim.admin;

import com.caoim.imcore.client.AdminImClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(exclude = {RedisAutoConfiguration.class, RedisRepositoriesAutoConfiguration.class})
@MapperScan("com.caoim.admin.dao")
@EnableFeignClients(clients = AdminImClient.class)
public class AdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
