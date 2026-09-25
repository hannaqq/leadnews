package com.news.user.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.news.apis.article.fallback")
public class InitConfig {
}
