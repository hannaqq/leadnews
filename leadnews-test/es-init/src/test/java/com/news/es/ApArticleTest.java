package com.news.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import com.news.es.pojo.SearchArticleVo;
import com.news.es.repository.ApArticleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.List;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@EnabledIfEnvironmentVariable(named = "RUN_ES_INIT", matches = "true")
public class ApArticleTest {
    @Autowired
    private ApArticleRepository apArticleRepository;

    @Autowired
    private ElasticsearchClient client;


    @Test
    public void init() throws IOException {
        List<SearchArticleVo> searchArticleVos = apArticleRepository.loadArticleList();

        BulkRequest.Builder builder = new BulkRequest.Builder();

        for (SearchArticleVo searchArticleVo : searchArticleVos) {
            builder.operations(op -> op
                    .index(idx -> idx
                            .index("app_info_article")
                            .id(searchArticleVo.getId().toString())
                            .document(searchArticleVo)));
        }
        BulkResponse result = client.bulk(builder.build());


    }
}
