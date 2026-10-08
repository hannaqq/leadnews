package com.news.wemedia;

import com.news.model.wemedia.pojos.WmChannel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EntityScan(basePackageClasses = WmChannel.class)
@EnableFeignClients(basePackages = "com.news.apis")
@EnableScheduling
public class WemediaApplication {

    public static void main(String[] args) {
        SpringApplication.run(WemediaApplication.class,args);
    }
}
