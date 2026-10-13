package com.news.wemedia.service.transaction;

import com.news.common.exception.CustomException;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.wemedia.pojos.WmMaterial;
import com.news.model.wemedia.pojos.WmNews;
import com.news.model.wemedia.pojos.WmNewsMaterial;
import com.news.wemedia.repository.WmMaterialRepository;
import com.news.wemedia.repository.WmNewsMaterialRepository;
import com.news.wemedia.repository.WmNewsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WmNewsTransactionService {
    private final WmNewsRepository newsRepository;
    private final WmMaterialRepository materialRepository;
    private final WmNewsMaterialRepository relationRepository;

    @Transactional
    public WmNews saveNewsAndRelations(WmNews incoming, Integer userId, List<String> contentUrls,
                                       List<String> coverUrls, short contentType, short coverType) {
        return saveNewsAndRelationsWithSchedule(
                incoming, userId, contentUrls, coverUrls, contentType, coverType).news();
    }

    @Transactional
    public SaveNewsResult saveNewsAndRelationsWithSchedule(
            WmNews incoming, Integer userId, List<String> contentUrls,
            List<String> coverUrls, short contentType, short coverType) {
        WmNews news;
        Long obsoleteTaskId = null;
        if (incoming.getId() == null) {
            incoming.setScheduleVersion(0L);
            news = newsRepository.save(incoming);
        } else {
            news = newsRepository.findByIdAndUserIdForUpdate(incoming.getId(), userId).orElseThrow();
            if (WmNews.Status.PROCESSING.getCode() == news.getStatus()
                    || WmNews.Status.PUBLISHING.getCode() == news.getStatus()
                    || WmNews.Status.PUBLISHED.getCode() == news.getStatus()) {
                throw new CustomException(AppHttpCodeEnum.PARAM_INVALID);
            }
            obsoleteTaskId = news.getScheduleTaskId();
            news.setScheduleTaskId(null);
            news.setScheduleVersion(nextScheduleVersion(news));
            BeanUtils.copyProperties(
                    incoming, news, "id", "articleId", "scheduleTaskId", "scheduleVersion");
            relationRepository.deleteByNewsId(news.getId());
        }
        saveRelations(news.getId(), userId, contentUrls, contentType);
        saveRelations(news.getId(), userId, coverUrls, coverType);
        return new SaveNewsResult(news, obsoleteTaskId);
    }

    @Transactional
    public Long deleteNews(Integer newsId, Integer userId) {
        WmNews news = newsRepository.findByIdAndUserId(newsId, userId).orElse(null);
        if (news == null) {
            return null;
        }
        relationRepository.deleteByNewsId(newsId);
        newsRepository.delete(news);
        return news.getArticleId();
    }

    private void saveRelations(Integer newsId, Integer userId, List<String> urls, short type) {
        if (urls == null || urls.isEmpty()) {
            return;
        }
        List<WmMaterial> materials = materialRepository.findByUserIdAndUrlIn(userId, urls);
        Map<String, WmMaterial> byUrl = new HashMap<>();
        for (WmMaterial material : materials) {
            byUrl.put(material.getUrl(), material);
        }
        if (byUrl.size() != urls.size()) {
            throw new CustomException(AppHttpCodeEnum.MATERIAL_REFERENCE_FAIL);
        }
        List<WmNewsMaterial> relations = new ArrayList<>(urls.size());
        for (int index = 0; index < urls.size(); index++) {
            WmMaterial material = byUrl.get(urls.get(index));
            if (material == null) {
                throw new CustomException(AppHttpCodeEnum.MATERIAL_REFERENCE_FAIL);
            }
            WmNewsMaterial relation = new WmNewsMaterial();
            relation.setNewsId(newsId);
            relation.setMaterialId(material.getId());
            relation.setType(type);
            relation.setOrd((short) index);
            relations.add(relation);
        }
        relationRepository.saveAll(relations);
    }

    private long nextScheduleVersion(WmNews news) {
        return news.getScheduleVersion() == null ? 1L : news.getScheduleVersion() + 1;
    }

    public record SaveNewsResult(WmNews news, Long obsoleteTaskId) {
    }
}
