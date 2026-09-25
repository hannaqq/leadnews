package com.news.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI springOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("leadnews API")
                        .description("leadnews API")
                        .version("1.0.0"));
    }
}
