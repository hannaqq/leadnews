package com.news.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.news.model.article.pojos.ApArticle;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.search.dtos.UserSearchDto;
import com.news.model.search.vos.SearchArticleVo;
import com.news.search.service.ApUserSearchService;
import com.news.search.service.ArticleSearchService;
import com.news.utils.thread.AppThreadLocalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class ArticleSearchServiceImpl implements ArticleSearchService {

    private final ElasticsearchClient elasticsearchClient;

    private final ApUserSearchService apUserSearchService;

    @Override
    public ResponseResult search(UserSearchDto userSearchDto) throws IOException {
        if(userSearchDto == null || StringUtils.isBlank(userSearchDto.getSearchWords())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        Integer userId = AppThreadLocalUtil.getUserId();

        if(userId != null && userSearchDto.getFromIndex() == 0){
            apUserSearchService.recordSearchHistory(userSearchDto.getSearchWords(), userId);
        }

        SearchResponse<SearchArticleVo> response = elasticsearchClient.search(s ->s
                .index("app_info_article")
                .query(queryBuilder -> queryBuilder
                        .bool(boolQueryBuilder -> boolQueryBuilder
                                .must(queryStringQueryBuilder -> queryStringQueryBuilder
                                        .multiMatch(m -> m
                                                .fields("title","content").query(userSearchDto.getSearchWords()))
                                )
                                .must(queryBuilder2 -> queryBuilder2
                                        .range(rangeQueryBuilder -> rangeQueryBuilder
                                                .date(dateRangeQueryBuilder -> dateRangeQueryBuilder
                                                        .field("publishTime")
                                                        .format("epoch_millis")
                                                        .lt(Long.toString(userSearchDto.getMinBehotTime().getTime()))))
                                )
                        )
                )
                .highlight(highlightBuilder ->highlightBuilder
                        .preTags("<font style='color: red; font-size: inherit;'>")
                        .postTags("</font>")
                        .fields("title",h ->h)
                )
                .from(0).size(userSearchDto.getPageSize())
                .sort(sortOptionsBuilder ->sortOptionsBuilder
                        .field(f ->f.field("publishTime").order(SortOrder.Desc))), SearchArticleVo.class);

        List<ApArticle> list = new ArrayList<>();

        List<Hit<SearchArticleVo>> hits = response.hits().hits();
        for (Hit<SearchArticleVo> hit : hits) {
            SearchArticleVo source = hit.source();
            if(hit.highlight() != null && !hit.highlight().isEmpty()){
                List<String> title = hit.highlight().get("title");
                source.setTitle(title.stream().collect(Collectors.joining()));
            }
            ApArticle apArticle = new ApArticle();
            BeanUtils.copyProperties(source, apArticle);
            list.add(apArticle);
        }

        return ResponseResult.okResult(list);
    }
}
