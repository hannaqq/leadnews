package com.news.article.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import com.news.article.mapper.ApArticleMapper;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;

import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class ArticleFreemarkerServiceImpl implements ArticleFreemarkerService {

    private final Configuration configuration;

    private final FileStorageService fileStorageService;

    private final ApArticleMapper apArticleMapper;

    private final KafkaTemplate<String,String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Override
    @Async
    @SneakyThrows
    public void buildArticleToMinIO(ApArticle apArticle, String content) {

        if(StringUtils.isNotBlank(content)){
            Template template = null;
            StringWriter out = new StringWriter();
            try {
                template = configuration.getTemplate("article.ftl");
                HashMap<String, Object> contentDataModel = new HashMap<>();
                contentDataModel.put("content", objectMapper.readValue(content, List.class));
                template.process(contentDataModel,out);
            } catch (Exception e) {
                e.printStackTrace();
            }

            InputStream in = new ByteArrayInputStream(out.toString().getBytes());
            String path = fileStorageService.uploadHtmlFile("", apArticle.getId() + ".html", in);

            apArticleMapper.update(apArticle,Wrappers.<ApArticle>lambdaUpdate().eq(ApArticle::getId,apArticle.getId()).set(ApArticle::getStaticUrl,path));

            createArticleESIndex(apArticle,content,path);

        }

    }

    @SneakyThrows
    private void createArticleESIndex(ApArticle apArticle, String content, String path) {
        SearchArticleVo searchArticleVo = new SearchArticleVo();
        BeanUtils.copyProperties(apArticle, searchArticleVo);
        searchArticleVo.setContent(content);
        searchArticleVo.setStaticUrl(path);

        kafkaTemplate.send(ArticleConstants.ARTICLE_ES_SYNC_TOPIC, objectMapper.writeValueAsString(searchArticleVo));



    }
}
