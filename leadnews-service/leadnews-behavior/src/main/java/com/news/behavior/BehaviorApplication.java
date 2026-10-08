package com.news.behavior;

import com.news.model.behavior.pojos.ApArticleReaction;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EntityScan(basePackageClasses = ApArticleReaction.class)
@EnableFeignClients(basePackages = "com.news.apis")
public class BehaviorApplication {
    public static void main(String[] args) {
        SpringApplication.run(BehaviorApplication.class, args);
    }
}
