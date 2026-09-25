package com.news.search.listener;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import com.news.common.constants.ArticleConstants;
import com.news.model.search.vos.SearchArticleVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;
@Component
@Slf4j
@RequiredArgsConstructor
public class SyncArticleListener {

    private final ElasticsearchClient esClient;

    private final ObjectMapper objectMapper;

    @KafkaListener(topics = ArticleConstants.ARTICLE_ES_SYNC_TOPIC)
    @SneakyThrows
    public void onMessage(String message) throws IOException {
        if(StringUtils.isNotBlank(message)){

            log.info("MESSAGE={}",message);
            SearchArticleVo searchArticleVo = objectMapper.readValue(message, SearchArticleVo.class);

            esClient.index(i -> i
                    .index("app_info_article")
                    .id(searchArticleVo.getId().toString())
                    .document(searchArticleVo));
        }

    }
}
