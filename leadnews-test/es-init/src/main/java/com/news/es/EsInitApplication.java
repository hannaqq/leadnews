package com.news.es;

import com.news.model.article.pojos.ApArticle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;

/**
 * @Description:
 * @Version: V1.0
 */
@SpringBootApplication
@EntityScan(basePackageClasses = ApArticle.class)
public class EsInitApplication {

    public static void main(String[] args) {
        SpringApplication.run(EsInitApplication.class, args);
    }

}
