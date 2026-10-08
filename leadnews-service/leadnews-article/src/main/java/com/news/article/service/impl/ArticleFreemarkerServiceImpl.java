package com.news.article.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import com.news.article.repository.ApArticleRepository;
import com.news.article.service.ArticleFreemarkerService;
import com.news.common.constants.ArticleConstants;
import com.news.file.service.FileStorageService;
import com.news.model.article.pojos.ApArticle;
import com.news.model.search.vos.SearchArticleVo;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleFreemarkerServiceImpl implements ArticleFreemarkerService {

    private final Configuration configuration;

    private final FileStorageService fileStorageService;

    private final ApArticleRepository articleRepository;

    private final KafkaTemplate<String,String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Override
    @Async
    public void buildArticleToMinIO(ApArticle apArticle, String content) {

        if(StringUtils.isNotBlank(content)){
            StringWriter out = new StringWriter();
            try {
                Template template = configuration.getTemplate("article.ftl");
                HashMap<String, Object> contentDataModel = new HashMap<>();
                contentDataModel.put("content", objectMapper.readValue(content, List.class));
                template.process(contentDataModel,out);
            } catch (Exception e) {
                throw new IllegalStateException("Failed to render article " + apArticle.getId(), e);
            }

            InputStream in = new ByteArrayInputStream(out.toString().getBytes(StandardCharsets.UTF_8));
            String path = fileStorageService.uploadHtmlFile("", apArticle.getId() + ".html", in);

            articleRepository.updateStaticUrl(apArticle.getId(), path);

            createArticleESIndex(apArticle,content,path);

        }

    }

    private void createArticleESIndex(ApArticle apArticle, String content, String path) {
        SearchArticleVo searchArticleVo = new SearchArticleVo();
        BeanUtils.copyProperties(apArticle, searchArticleVo);
        searchArticleVo.setContent(content);
        searchArticleVo.setStaticUrl(path);

        try {
            String message = objectMapper.writeValueAsString(searchArticleVo);
            kafkaTemplate.send(ArticleConstants.ARTICLE_ES_SYNC_TOPIC, message)
                    .whenComplete((result, exception) -> {
                        if (exception != null) {
                            log.error("Failed to publish article index event for article {}", apArticle.getId(), exception);
                        }
                    });
        } catch (JsonProcessingException exception) {
            log.error("Failed to serialize article index event for article {}", apArticle.getId(), exception);
        }
    }
}
