package com.news.wemedia.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.news.apis.article.IArticleClient;
import com.news.common.constants.WemediaConstants;
import com.news.model.article.dtos.ArticleDto;
import com.news.model.common.dtos.PageResponseResult;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.wemedia.dtos.NewsAuthDto;
import com.news.model.wemedia.dtos.NewsDto;
import com.news.model.wemedia.dtos.WmNewsDto;
import com.news.model.wemedia.dtos.WmNewsPageReqDto;
import com.news.model.wemedia.pojos.WmChannel;
import com.news.model.wemedia.pojos.WmNews;
import com.news.model.wemedia.pojos.WmUser;
import com.news.utils.thread.WmThreadLocalUtil;
import com.news.wemedia.repository.WmChannelRepository;
import com.news.wemedia.repository.WmNewsRepository;
import com.news.wemedia.repository.WmUserRepository;
import com.news.wemedia.service.WmNewsService;
import com.news.wemedia.service.WmNewsTaskService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WmNewsServiceImpl implements WmNewsService {
    private final WmNewsRepository newsRepository;
    private final WmUserRepository userRepository;
    private final WmChannelRepository channelRepository;
    private final WemediaPersistenceService persistenceService;
    private final WmNewsTaskService newsTaskService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final IArticleClient articleClient;
    private final ObjectMapper objectMapper;

    @Override
    public ResponseResult getList(WmNewsPageReqDto dto) {
        dto.checkParam();
        PageRequest request = PageRequest.of(dto.getPage() - 1, dto.getSize(),
                Sort.by(Sort.Direction.DESC, "publishTime"));
        Page<WmNews> page = newsRepository.findForUser(
                WmThreadLocalUtil.getUser().getId(), dto.getStatus(), dto.getChannelId(),
                StringUtils.trimToNull(dto.getKeyword()), dto.getBeginPubDate(), dto.getEndPubDate(), request);
        ResponseResult result = new PageResponseResult(dto.getPage(), dto.getSize(),
                Math.toIntExact(page.getTotalElements()));
        result.setData(page.getContent());
        return result;
    }

    @Override
    public ResponseResult submit(WmNewsDto dto) {
        if (dto == null || dto.getContent() == null || dto.getStatus() == null || dto.getType() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        List<String> contentImages = extractUrlInfo(dto.getContent());
        List<String> coverImages = dto.getImages() == null ? new ArrayList<>() : new ArrayList<>(dto.getImages());

        WmNews news = new WmNews();
        BeanUtils.copyProperties(dto, news);
        news.setUserId(WmThreadLocalUtil.getUser().getId());
        news.setCreatedTime(new Date());
        news.setSubmitedTime(new Date());
        news.setEnable((short) 1);

        if (dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)) {
            if (contentImages.size() >= 3) {
                news.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
                coverImages = new ArrayList<>(contentImages.subList(0, 3));
            } else if (contentImages.isEmpty()) {
                news.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
                coverImages = new ArrayList<>();
            } else {
                news.setType(WemediaConstants.WM_NEWS_SINGLE_IMAGE);
                coverImages = new ArrayList<>(contentImages.subList(0, 1));
            }
        }
        news.setImages(coverImages.isEmpty() ? null : StringUtils.join(coverImages, ","));

        boolean draft = dto.getStatus().equals(WmNews.Status.NORMAL.getCode());
        if (!draft && dto.getPublishTime() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "publish time is required");
        }
        WmNews saved = persistenceService.saveNewsAndRelations(
                news,
                news.getUserId(),
                draft ? List.of() : contentImages,
                draft ? List.of() : coverImages,
                WemediaConstants.WM_CONTENT_REFERENCE,
                WemediaConstants.WM_COVER_REFERENCE);
        if (!draft) {
            newsTaskService.addNewsToTask(saved.getId(), saved.getPublishTime());
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    @SneakyThrows
    public ResponseResult downOrUp(WmNewsDto dto) {
        if (dto.getId() == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        WmNews news = newsRepository.findByIdAndUserId(
                dto.getId(), WmThreadLocalUtil.getUser().getId()).orElse(null);
        if (news == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "article doesn't exist");
        }
        if (!news.getStatus().equals(WmNews.Status.PUBLISHED.getCode())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "article hasn't been published");
        }
        if (dto.getEnable() != null && dto.getEnable() > -1 && dto.getEnable() < 2) {
            news.setEnable(dto.getEnable());
            newsRepository.save(news);
            if (news.getArticleId() != null) {
                Map<String, Object> event = new HashMap<>();
                event.put("articleId", news.getArticleId());
                event.put("enable", dto.getEnable());
                kafkaTemplate.send("wm.news.topic.down.or.up", objectMapper.writeValueAsString(event));
            }
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult getList_vo(NewsAuthDto dto) {
        dto.checkParam();
        PageRequest request = PageRequest.of(dto.getPage() - 1, dto.getSize(),
                Sort.by(Sort.Direction.DESC, "createdTime"));
        Short status = dto.getStatus() == null ? null : dto.getStatus().shortValue();
        Page<WmNews> page = newsRepository.findForReview(StringUtils.trimToNull(dto.getTitle()), status, request);
        List<NewsDto> data = page.getContent().stream().map(this::toNewsDto).toList();
        ResponseResult result = new PageResponseResult(dto.getPage(), dto.getSize(),
                Math.toIntExact(page.getTotalElements()));
        result.setData(data);
        return result;
    }

    @Override
    public ResponseResult getDetail(Integer id) {
        WmNews news = newsRepository.findById(id).orElse(null);
        return news == null
                ? ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST)
                : ResponseResult.okResult(toNewsDto(news));
    }

    @Override
    public ResponseResult authFail(NewsAuthDto dto) {
        WmNews news = newsRepository.findById(dto.getId()).orElseThrow();
        news.setStatus(WmNews.Status.FAIL.getCode());
        news.setReason(dto.getMsg());
        return ResponseResult.okResult(newsRepository.save(news));
    }

    @Override
    public ResponseResult authPass(NewsAuthDto dto) {
        WmNews news = newsRepository.findById(dto.getId()).orElseThrow();
        ResponseResult articleResult = saveAppArticle(news);
        if (!Integer.valueOf(AppHttpCodeEnum.SUCCESS.getCode()).equals(articleResult.getCode())) {
            throw new IllegalStateException("Failed to save app article");
        }
        news.setStatus(WmNews.Status.ADMIN_SUCCESS.getCode());
        news.setReason("auth processed");
        news.setArticleId((Long) articleResult.getData());
        return ResponseResult.okResult(newsRepository.save(news));
    }

    @Override
    public ResponseResult delNews(Integer id) {
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        Long articleId = persistenceService.deleteNews(id, WmThreadLocalUtil.getUser().getId());
        if (articleId != null) {
            articleClient.delArticle(articleId);
        }
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode());
    }

    @Override
    public ResponseResult getOne(Integer id) {
        return ResponseResult.okResult(newsRepository.findByIdAndUserId(
                id, WmThreadLocalUtil.getUser().getId()).orElse(null));
    }

    @Override
    public boolean existsByChannelId(Integer channelId) {
        return newsRepository.existsByChannelId(channelId);
    }

    private NewsDto toNewsDto(WmNews news) {
        NewsDto dto = new NewsDto();
        BeanUtils.copyProperties(news, dto);
        userRepository.findById(news.getUserId()).ifPresent(user -> dto.setAuthorName(user.getName()));
        return dto;
    }

    private ResponseResult saveAppArticle(WmNews news) {
        WmUser user = userRepository.findById(news.getUserId()).orElse(null);
        WmChannel channel = channelRepository.findById(news.getChannelId()).orElse(null);
        ArticleDto dto = new ArticleDto();
        BeanUtils.copyProperties(news, dto);
        dto.setLayout(news.getType());
        dto.setAuthorId(news.getUserId().longValue());
        if (user != null) dto.setAuthorName(user.getName());
        if (channel != null) dto.setChannelName(channel.getName());
        dto.setId(news.getArticleId());
        dto.setCreatedTime(new Date());
        return articleClient.saveArticle(dto);
    }

    @SneakyThrows
    private List<String> extractUrlInfo(String content) {
        List<String> materials = new ArrayList<>();
        List<Map<String, Object>> blocks = objectMapper.readValue(content, new TypeReference<>() {});
        for (Map<String, Object> block : blocks) {
            if ("image".equals(block.get("type"))) {
                materials.add((String) block.get("value"));
            }
        }
        return materials;
    }
}
