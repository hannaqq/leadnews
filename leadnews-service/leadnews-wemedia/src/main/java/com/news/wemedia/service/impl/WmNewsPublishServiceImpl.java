package com.news.wemedia.service.impl;

import com.news.apis.article.IArticleClient;
import com.news.apis.schedule.IScheduleClient;
import com.news.model.article.dtos.ArticleDto;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.common.enums.TaskTypeEnum;
import com.news.model.schedule.dtos.Task;
import com.news.model.wemedia.dtos.NewsPublishTaskPayload;
import com.news.model.wemedia.pojos.WmChannel;
import com.news.model.wemedia.pojos.WmNews;
import com.news.model.wemedia.pojos.WmUser;
import com.news.utils.common.ProtostuffUtil;
import com.news.wemedia.repository.WmChannelRepository;
import com.news.wemedia.repository.WmNewsRepository;
import com.news.wemedia.repository.WmUserRepository;
import com.news.wemedia.service.PublicationScheduleException;
import com.news.wemedia.service.WmNewsPublishService;
import com.news.wemedia.service.transaction.WmNewsScheduleTransactionService;
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
    private final WmNewsScheduleTransactionService scheduleTransactionService;

    @Override
    public void reviewApproved(WmNews news) {
        Date publishTime = news.getPublishTime();
        if (publishTime == null || !publishTime.after(new Date())) {
            if (publishTime == null) {
                news.setPublishTime(new Date());
            }
            publishImmediately(news);
            return;
        }

        WmNewsScheduleTransactionService.ScheduleChange change =
                scheduleTransactionService.prepareApprovedSchedule(news.getId(), publishTime);
        createAndAttachTask(change);
    }

    @Override
    public void reschedule(Integer newsId, Integer userId, Date publishTime) {
        WmNewsScheduleTransactionService.ScheduleChange change =
                scheduleTransactionService.reschedule(newsId, userId, publishTime);
        cancelObsoleteTask(change.obsoleteTaskId());
        createAndAttachTask(change);
    }

    @Override
    public void cancelSchedule(Integer newsId, Integer userId) {
        cancelObsoleteTask(scheduleTransactionService.cancelSchedule(newsId, userId));
    }

    @Override
    public void cancelObsoleteTask(Long taskId) {
        if (taskId == null) {
            return;
        }
        try {
            ResponseResult result = scheduleClient.cancelTask(taskId);
            if (result == null || !Integer.valueOf(AppHttpCodeEnum.SUCCESS.getCode()).equals(result.getCode())) {
                log.warn("Schedule service did not confirm cancellation of task {}", taskId);
            }
        } catch (RuntimeException exception) {
            log.warn("Failed to remove obsolete schedule task {}", taskId, exception);
        }
    }

    @Override
    public void publishScheduled(Integer newsId, Date expectedPublishTime, long scheduleVersion) {
        WmNews news = scheduleTransactionService.claimPublication(
                newsId, expectedPublishTime, scheduleVersion).orElse(null);
        if (news == null) {
            return;
        }
        try {
            long articleId = saveArticle(news);
            scheduleTransactionService.completePublication(newsId, scheduleVersion, articleId);
        } catch (RuntimeException exception) {
            log.error("Failed to publish approved news {}", newsId, exception);
            if (scheduleTransactionService.restoreWaitingForRetry(newsId, scheduleVersion)) {
                createAndAttachRetryTask(newsId, expectedPublishTime, scheduleVersion);
            }
        }
    }

    private void publishImmediately(WmNews news) {
        long articleId = saveArticle(news);
        news.setArticleId(articleId);
        news.setStatus(WmNews.Status.PUBLISHED.getCode());
        news.setReason("processed");
        newsRepository.save(news);
    }

    private long saveArticle(WmNews news) {
        ResponseResult result = articleClient.saveArticle(toArticleDto(news));
        if (result == null
                || !Integer.valueOf(AppHttpCodeEnum.SUCCESS.getCode()).equals(result.getCode())
                || !(result.getData() instanceof Number articleId)) {
            throw new IllegalStateException("failed to save app article");
        }
        return articleId.longValue();
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

    private void createAndAttachTask(WmNewsScheduleTransactionService.ScheduleChange change) {
        try {
            long taskId = addTask(
                    change.publishTime(),
                    new NewsPublishTaskPayload(
                            change.newsId(), change.publishTime(), change.scheduleVersion()));
            if (!scheduleTransactionService.attachTask(
                    change.newsId(), change.scheduleVersion(), taskId)) {
                cancelObsoleteTask(taskId);
                throw new PublicationScheduleException("schedule changed while task was being created");
            }
        } catch (RuntimeException exception) {
            scheduleTransactionService.markScheduleCreationFailed(
                    change.newsId(), change.scheduleVersion());
            if (exception instanceof PublicationScheduleException scheduleException) {
                throw scheduleException;
            }
            throw new PublicationScheduleException("failed to create publication task", exception);
        }
    }

    private void createAndAttachRetryTask(
            Integer newsId, Date expectedPublishTime, long scheduleVersion) {
        try {
            long taskId = addTask(
                    new Date(System.currentTimeMillis() + PUBLISH_RETRY_DELAY_MILLIS),
                    new NewsPublishTaskPayload(newsId, expectedPublishTime, scheduleVersion));
            if (!scheduleTransactionService.attachTask(newsId, scheduleVersion, taskId)) {
                cancelObsoleteTask(taskId);
            }
        } catch (RuntimeException exception) {
            scheduleTransactionService.markScheduleCreationFailed(newsId, scheduleVersion);
            log.error("Failed to schedule publication retry for news {}", newsId, exception);
        }
    }

    private long addTask(Date executeTime, NewsPublishTaskPayload payload) {
        Task task = new Task();
        task.setExecuteTime(executeTime.getTime());
        task.setTaskType(TaskTypeEnum.NEWS_PUBLISH.getTaskType());
        task.setPriority(TaskTypeEnum.NEWS_PUBLISH.getPriority());
        task.setParameters(ProtostuffUtil.serialize(payload));

        ResponseResult result = scheduleClient.addTask(task);
        if (result == null
                || !Integer.valueOf(AppHttpCodeEnum.SUCCESS.getCode()).equals(result.getCode())
                || !(result.getData() instanceof Number taskId)) {
            throw new IllegalStateException("failed to schedule publication");
        }
        return taskId.longValue();
    }
}
