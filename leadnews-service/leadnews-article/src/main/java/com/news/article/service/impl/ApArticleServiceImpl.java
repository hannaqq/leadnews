package com.news.article.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.news.article.mapper.ApArticleConfigMapper;
import com.news.article.mapper.ApArticleContentMapper;
import com.news.article.mapper.ApArticleMapper;
import com.news.article.service.ApArticleService;
import com.news.article.service.ArticleFreemarkerService;
import com.news.common.constants.ArticleConstants;
import com.news.model.article.dtos.ArticleDto;
import com.news.model.article.dtos.ArticleHomeDto;
import com.news.model.article.dtos.ArticleInfoDto;
import com.news.model.article.pojos.ApArticle;
import com.news.model.article.pojos.ApArticleConfig;
import com.news.model.article.pojos.ApArticleContent;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {

    private final ApArticleMapper apArticleMapper;

    private final static short MAX_PAGE_SIZE = 50;

    @Override
    public ResponseResult load(ArticleHomeDto dto, Short type) {

        Integer size = dto.getSize();
        if(size == null || size == 0){
            size = 7;
        }
        size = Math.min(size,MAX_PAGE_SIZE);
        dto.setSize(size);

        if(!type.equals(ArticleConstants.LOADTYPE_LOAD_MORE) && !type.equals(ArticleConstants.LOADTYPE_LOAD_NEW)){
            type = ArticleConstants.LOADTYPE_LOAD_MORE;
        }

        if(StringUtils.isBlank(dto.getTag())){
            dto.setTag(ArticleConstants.DEFAULT_TAG);
        }

        if(dto.getMaxBehotTime() == null){
            dto.setMaxBehotTime(new Date());
        }
        if(dto.getMinBehotTime() == null){
            dto.setMinBehotTime(new Date());
        }

        List<ApArticle> apArticleList = apArticleMapper.loadArticleList(dto, type);
        return ResponseResult.okResult(apArticleList);
    }

    private final ApArticleConfigMapper apArticleConfigMapper;

    private final ApArticleContentMapper apArticleContentMapper;

    private final ArticleFreemarkerService articleFreemarkerService;

    @Override
    public ResponseResult saveArticle(ArticleDto dto) {
        if (dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        ApArticle apArticle = new ApArticle();
        BeanUtils.copyProperties(dto,apArticle);

        if(dto.getId() == null){
            save(apArticle);

            ApArticleConfig apArticleConfig = new ApArticleConfig();
            apArticleConfig.setArticleId(apArticle.getId());
            apArticleConfig.setIsComment(true);
            apArticleConfig.setIsDelete(false);
            apArticleConfig.setIsDown(false);
            apArticleConfig.setIsForward(true);

            apArticleConfigMapper.insert(apArticleConfig);

            ApArticleContent apArticleContent = new ApArticleContent();
            apArticleContent.setArticleId(apArticle.getId());
            apArticleContent.setContent(dto.getContent());
            apArticleContentMapper.insert(apArticleContent);

        }else {
            updateById(apArticle);
            ApArticleContent apArticleContent = apArticleContentMapper.selectOne(Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId,apArticle.getId()));
            apArticleContent.setContent(dto.getContent());
            apArticleContentMapper.updateById(apArticleContent);
        }

        articleFreemarkerService.buildArticleToMinIO(apArticle,dto.getContent());
        return ResponseResult.okResult(apArticle.getId());
    }

    @Override
    public ResponseResult delArticle(Long id) {

        removeById(id);
        ApArticleConfig apArticleConfig = apArticleConfigMapper.selectOne(Wrappers.<ApArticleConfig>lambdaQuery().eq(ApArticleConfig::getArticleId, id));
        apArticleConfig.setIsDelete(true);
        apArticleConfigMapper.updateById(apArticleConfig);

        apArticleContentMapper.delete(Wrappers.<ApArticleContent>lambdaQuery().eq(ApArticleContent::getArticleId, id));

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode());
    }

    @Override
    public ResponseResult loadArticleBehavior(ArticleInfoDto dto) {
        return null;
    }


}
