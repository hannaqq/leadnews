package com.news.article.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.article.repository.ApArticleRepository;
import com.news.file.service.FileStorageService;
import com.news.model.article.pojos.ApArticle;
import freemarker.template.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ArticleFreemarkerServiceImplTest {

    @Test
    void stopsBeforeUploadAndIndexingWhenRenderingFails() throws Exception {
        Configuration configuration = mock(Configuration.class);
        FileStorageService storageService = mock(FileStorageService.class);
        ApArticleRepository articleRepository = mock(ApArticleRepository.class);
        @SuppressWarnings("unchecked")
        KafkaTemplate<String, String> kafkaTemplate = mock(KafkaTemplate.class);
        when(configuration.getTemplate("article.ftl"))
                .thenThrow(new IOException("template unavailable"));
        ArticleFreemarkerServiceImpl service = new ArticleFreemarkerServiceImpl(
                configuration, storageService, articleRepository, kafkaTemplate, new ObjectMapper());
        ApArticle article = new ApArticle();
        article.setId(1L);

        assertThrows(IllegalStateException.class,
                () -> service.buildArticleToMinIO(article, "[]"));

        verify(storageService, never()).uploadHtmlFile(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any());
        verify(articleRepository, never()).updateStaticUrl(
                org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyString());
        verify(kafkaTemplate, never()).send(
                org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
    }
}
