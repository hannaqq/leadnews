package com.news.wemedia.service.impl;

import com.news.apis.article.IArticleClient;
import com.news.apis.schedule.IScheduleClient;
import com.news.model.article.dtos.ArticleDto;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.common.enums.TaskTypeEnum;
import com.news.model.schedule.dtos.Task;
import com.news.model.wemedia.pojos.WmChannel;
import com.news.model.wemedia.pojos.WmNews;
import com.news.model.wemedia.pojos.WmUser;
import com.news.utils.common.ProtostuffUtil;
import com.news.wemedia.repository.WmChannelRepository;
import com.news.wemedia.repository.WmNewsRepository;
import com.news.wemedia.repository.WmUserRepository;
import com.news.wemedia.service.WmNewsPublishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class WmNewsPublishServiceImpl implements WmNewsPublishService {

    private static final long PUBLISH_RETRY_DELAY_MILLIS = 60_000L;

    private final IScheduleClient scheduleClient;
    private final IArticleClient articleClient;
    private final WmNewsRepository newsRepository;
    private final WmUserRepository userRepository;
    private final WmChannelRepository channelRepository;

    @Override
    public void reviewApproved(WmNews news) {
        Date publishTime = news.getPublishTime();
        if (publishTime == null || !publishTime.after(new Date())) {
            if (publishTime == null) {
                news.setPublishTime(new Date());
            }
            publishFromProcessing(news);
            return;
        }

        WmNews payload = new WmNews();
        payload.setId(news.getId());
        payload.setPublishTime(publishTime);
        addTask(TaskTypeEnum.NEWS_PUBLISH, publishTime, payload);
        news.setStatus(WmNews.Status.SUCCESS.getCode());
        news.setReason("approved and waiting for publication");
        newsRepository.save(news);
    }

    @Override
    public void publishScheduled(Integer newsId, Date expectedPublishTime) {
        WmNews news = newsRepository.findById(newsId).orElse(null);
        if (news == null
                || expectedPublishTime == null
                || news.getStatus() != WmNews.Status.SUCCESS.getCode()
                || news.getPublishTime() == null
                || news.getPublishTime().getTime() != expectedPublishTime.getTime()) {
            return;
        }
        try {
            publishFromProcessing(news);
        } catch (RuntimeException exception) {
            log.error("Failed to publish approved news {}", newsId, exception);
            news.setStatus(WmNews.Status.SUCCESS.getCode());
            news.setReason("publication failed; waiting to retry");
            newsRepository.save(news);
            WmNews payload = new WmNews();
            payload.setId(newsId);
            payload.setPublishTime(expectedPublishTime);
            addTask(
                    TaskTypeEnum.NEWS_PUBLISH,
                    new Date(System.currentTimeMillis() + PUBLISH_RETRY_DELAY_MILLIS),
                    payload);
        }
    }

    private void publishFromProcessing(WmNews news) {
        ResponseResult result = articleClient.saveArticle(toArticleDto(news));
        if (!Integer.valueOf(AppHttpCodeEnum.SUCCESS.getCode()).equals(result.getCode())
                || !(result.getData() instanceof Number articleId)) {
            throw new IllegalStateException("failed to save app article");
        }
        news.setArticleId(articleId.longValue());
        news.setStatus(WmNews.Status.PUBLISHED.getCode());
        news.setReason("processed");
        newsRepository.save(news);
    }

    private ArticleDto toArticleDto(WmNews news) {
        WmUser user = userRepository.findById(news.getUserId()).orElse(null);
        WmChannel channel = channelRepository.findById(news.getChannelId()).orElse(null);

        ArticleDto dto = new ArticleDto();
        BeanUtils.copyProperties(news, dto);
        dto.setId(news.getArticleId());
        dto.setSourceNewsId(news.getId());
        dto.setLayout(news.getType());
        dto.setAuthorId(news.getUserId().longValue());
        if (user != null) {
            dto.setAuthorName(user.getName());
        }
        if (channel != null) {
            dto.setChannelName(channel.getName());
        }
        dto.setCreatedTime(new Date());
        return dto;
    }

    private void addTask(TaskTypeEnum type, Date executeTime, WmNews payload) {
        Task task = new Task();
        task.setExecuteTime(executeTime.getTime());
        task.setTaskType(type.getTaskType());
        task.setPriority(type.getPriority());
        task.setParameters(ProtostuffUtil.serialize(payload));

        ResponseResult result = scheduleClient.addTask(task);
        if (result == null || !Integer.valueOf(AppHttpCodeEnum.SUCCESS.getCode()).equals(result.getCode())) {
            throw new IllegalStateException("failed to schedule " + type.getDesc());
        }
    }
}
