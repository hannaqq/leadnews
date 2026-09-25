package com.news.article.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.news.article.mapper.ApArticleConfigMapper;
import com.news.article.service.ApArticleConfigService;
import com.news.model.article.pojos.ApArticleConfig;
import org.springframework.stereotype.Service;

import java.util.Map;


@Service
public class ApArticleConfigServiceImpl extends ServiceImpl<ApArticleConfigMapper, ApArticleConfig> implements ApArticleConfigService {
    @Override
    public void updateByMap(Map map) {
        Object enable = map.get("enable");
        boolean isDown = true;
        if(enable.equals(1)){
             isDown = false;
        }
        update(Wrappers.<ApArticleConfig>lambdaUpdate().eq(ApArticleConfig::getArticleId,map.get("articleId"))
                .set(ApArticleConfig::getIsDown,isDown));
    }
}
