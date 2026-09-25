package com.news.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import com.news.es.mapper.ApArticleMapper;
import com.news.es.pojo.SearchArticleVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.IOException;
import java.util.List;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class ApArticleTest {
    @Autowired
    private ApArticleMapper apArticleMapper;

    @Autowired
    private ElasticsearchClient client;


    @Test
    public void init() throws IOException {
        List<SearchArticleVo> searchArticleVos = apArticleMapper.loadArticleList();

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
