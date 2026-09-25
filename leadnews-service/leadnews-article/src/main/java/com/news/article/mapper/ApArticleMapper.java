package com.news.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.news.model.article.dtos.ArticleHomeDto;
import com.news.model.article.pojos.ApArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ApArticleMapper extends BaseMapper<ApArticle> {

    /**
     *
     * @param dto
     * @param type  1 loadmore   2 loadnew
     * @return
     */
    public List<ApArticle> loadArticleList(@Param("dto") ArticleHomeDto dto, @Param("type") Short type);
}
