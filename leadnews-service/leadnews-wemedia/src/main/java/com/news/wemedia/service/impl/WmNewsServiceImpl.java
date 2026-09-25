package com.news.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.news.apis.article.IArticleClient;
import com.news.common.constants.WemediaConstants;
import com.news.common.exception.CustomException;
import com.news.model.article.dtos.ArticleDto;
import com.news.model.common.dtos.PageResponseResult;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.wemedia.dtos.NewsAuthDto;
import com.news.model.wemedia.dtos.NewsDto;
import com.news.model.wemedia.dtos.WmNewsDto;
import com.news.model.wemedia.dtos.WmNewsPageReqDto;
import com.news.model.wemedia.pojos.*;
import com.news.utils.thread.WmThreadLocalUtil;
import com.news.wemedia.mapper.*;
import com.news.wemedia.service.WmMaterialService;
import com.news.wemedia.service.WmNewsAutoScanService;
import com.news.wemedia.service.WmNewsService;
import com.news.wemedia.service.WmNewsTaskService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
@Service
@Slf4j
@Transactional
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {

    @Override
    public ResponseResult getList(WmNewsPageReqDto dto) {
        dto.checkParam();
        IPage page = new Page(dto.getPage(), dto.getSize());
        LambdaQueryWrapper<WmNews> lqw = new LambdaQueryWrapper<>();
        if(dto.getStatus() != null){
            lqw.eq(WmNews::getStatus,dto.getStatus());
        }
        if(dto.getChannelId() != null){
            lqw.eq(WmNews::getChannelId,dto.getChannelId());
        }
        if(dto.getKeyword() != null){
            lqw.like(WmNews::getTitle,dto.getKeyword());
        }
        if(dto.getBeginPubDate() != null && dto.getEndPubDate() !=null){
            lqw.between(WmNews::getPublishTime,dto.getBeginPubDate(),dto.getEndPubDate());
        }

        lqw.eq(WmNews::getUserId, WmThreadLocalUtil.getUser().getId())
                .orderByDesc(WmNews::getPublishTime);

        page = page(page,lqw);


        ResponseResult responseResult = new PageResponseResult(dto.getPage(),dto.getSize(),(int)page.getTotal());
        responseResult.setData(page.getRecords());

        return responseResult;
    }

    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;

    @Autowired
    private WmMaterialMapper wmMaterialMapper;

    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;

    @Autowired
    private WmNewsTaskService wmNewsTaskService;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @Autowired
    private WmUserMapper wmUserMapper;

    @Autowired
    private IArticleClient iArticleClient;

    @Autowired
    private WmChannelMapper wmChannelMapper;

    @Autowired
    private WmMaterialService wmMaterialService;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ResponseResult submit(WmNewsDto dto) {
        if(dto ==null ||dto.getContent() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        WmNews wmNews = new WmNews();
        BeanUtils.copyProperties(dto,wmNews);
        if(dto.getImages() != null && dto.getImages().size() != 0){
            String imageStr = StringUtils.join(dto.getImages(), ",");
            wmNews.setImages(imageStr);
        }

        if(dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)){
            wmNews.setType(null);
        }

        saveOrUpdateWmNews(wmNews);

        if(dto.getStatus().equals(WmNews.Status.NORMAL.getCode())){
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }

        List<String> materials = extractUrlInfo(dto.getContent());

        saveRelativeInfoForContent(materials,wmNews.getId());
        saveRelativeInfoForCover(dto,wmNews,materials);

        wmNewsTaskService.addNewsToTask(wmNews.getId(),wmNews.getPublishTime());

        //wmNewsAutoScanService.autoScanWmNews(wmNews.getId());

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult downOrUp(WmNewsDto dto) {
        if(dto.getId() == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }

        WmNews wmNews = getById(dto.getId());
        if(wmNews == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST,"article doesn't exist");
        }

        if(!wmNews.getStatus().equals(WmNews.Status.PUBLISHED.getCode())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"article hasn't been published");
        }

        if(dto.getEnable() !=null && dto.getEnable() > -1 && dto.getEnable() <2){
            update(Wrappers.<WmNews>lambdaUpdate().set(WmNews::getEnable,dto.getEnable())
                    .eq(WmNews::getId,wmNews.getId()));

            if(wmNews.getArticleId() != null){
                Map<String,Object> map = new HashMap<>();
                map.put("articleId",wmNews.getArticleId());
                map.put("enable",dto.getEnable());
                kafkaTemplate.send("wm.news.topic.down.or.up",objectMapper.writeValueAsString(map));
            }
        }



        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult getList_vo(NewsAuthDto dto) {
        dto.checkParam();
        IPage page = new Page(dto.getPage(),dto.getSize());
        LambdaQueryWrapper<WmNews> lqw = new LambdaQueryWrapper<>();
        lqw.orderByDesc(WmNews::getCreatedTime);
        if(StringUtils.isNotBlank(dto.getTitle())){
            lqw.eq(WmNews::getTitle,dto.getTitle());
        }
        if(dto.getStatus() !=null){
            lqw.eq(WmNews::getStatus, dto.getStatus());
        }

        page = page(page,lqw);

        ArrayList<NewsDto> newsDtos = new ArrayList<>();

        for (Object record : page.getRecords()) {
            if(record instanceof WmNews){
                WmNews wmNews = (WmNews) record;
                NewsDto newsDto = new NewsDto();
                BeanUtils.copyProperties(wmNews, newsDto);
                WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
                newsDto.setAuthorName(wmUser.getName());
                newsDtos.add(newsDto);
            }
        }

        ResponseResult responseResult = new PageResponseResult(dto.getPage(),dto.getSize(),(int)page.getTotal());
        responseResult.setData(newsDtos);

        return responseResult;
    }

    @Override
    public ResponseResult getDetail(Integer id) {
        WmNews wmNews = getById(id);
        NewsDto newsDto = new NewsDto();
        BeanUtils.copyProperties(wmNews, newsDto);
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        newsDto.setAuthorName(wmUser.getName());
        return ResponseResult.okResult(newsDto);
    }

    @Override
    public ResponseResult authFail(NewsAuthDto dto) {
        WmNews wmNews = getById(dto.getId());
        wmNews.setStatus((short)2);
        wmNews.setReason(dto.getMsg());
        updateById(wmNews);

        return ResponseResult.okResult(wmNews);
    }

    @Override
    public ResponseResult authPass(NewsAuthDto dto) {
        WmNews wmNews = getById(dto.getId());
        wmNews.setStatus((short)4);
        wmNews.setReason("auth processed");

        ResponseResult responseResult = saveAppArticle(wmNews);
        if(!responseResult.getCode().equals(200)){
            throw new RuntimeException("save failed");
        }
        wmNews.setArticleId((Long) responseResult.getData());
        updateById(wmNews);

        return ResponseResult.okResult(wmNews);
    }

    @Override
    public ResponseResult delNews(Integer id) {
        if(id == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        WmNews wmNews = getById(id);
        removeById(id);
        List<WmNewsMaterial> wmNewsMaterials = wmNewsMaterialMapper.selectList(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getNewsId, id));
        for (WmNewsMaterial wmNewsMaterial : wmNewsMaterials) {
            wmNewsMaterialMapper.deleteById(wmNewsMaterial);
        }

        if(wmNews.getArticleId() != null){
            iArticleClient.delArticle(wmNews.getArticleId());
        }

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS.getCode());
    }

    @Override
    public ResponseResult getOne(Integer id) {
        WmNews wmNews = getById(id);
        return ResponseResult.okResult(wmNews);
    }

    private ResponseResult saveAppArticle(WmNews wmNews) {
        WmUser wmUser = wmUserMapper.selectById(wmNews.getUserId());
        WmChannel wmChannel = wmChannelMapper.selectById(wmNews.getChannelId());

        ArticleDto dto = new ArticleDto();
        BeanUtils.copyProperties(wmNews,dto);
        dto.setLayout(wmNews.getType());
        dto.setAuthorId(wmNews.getUserId().longValue());
        if(wmUser != null){
            dto.setAuthorName(wmUser.getName());
        }
        if(wmChannel != null){
            dto.setChannelName(wmChannel.getName());
        }

        if(wmNews.getArticleId() != null){
            dto.setId(wmNews.getArticleId());
        }

        dto.setCreatedTime(new Date());

        ResponseResult responseResult = iArticleClient.saveArticle(dto);
        return responseResult;

    }

    private void saveOrUpdateWmNews(WmNews wmNews){
        wmNews.setUserId(WmThreadLocalUtil.getUser().getId());
        wmNews.setCreatedTime(new Date());
        wmNews.setSubmitedTime(new Date());
        wmNews.setEnable((short)1);

        if(wmNews.getId() == null){
            save(wmNews);
        }else {
            wmNewsMaterialMapper.delete(Wrappers.<WmNewsMaterial>lambdaQuery().eq(WmNewsMaterial::getNewsId,wmNews.getId()));
            updateById(wmNews);
        }
    }
    private List<String> extractUrlInfo(String content){
        List<String> materials = new ArrayList<>();
        List<Map> maps = objectMapper.readValue(content, new TypeReference<List<Map<String, Object>>>() {});
        for(Map map : maps){
            if(map.get("type").equals("image")){
                String imgUrl = (String)map.get("value");
                materials.add(imgUrl);
            }
        }
        return materials;
    }

    private void saveRelativeInfoForContent(List<String> materials,Integer newsId){
        saveRelativeInfo(materials,newsId,WemediaConstants.WM_CONTENT_REFERENCE);
    }

    private void saveRelativeInfo(List<String> materials,Integer newsId,Short type){
        if(materials !=null && !materials.isEmpty()) {
            List<WmMaterial> dbMaterials = wmMaterialMapper.selectList(Wrappers.<WmMaterial>lambdaQuery().in(WmMaterial::getUrl,materials));
            if (dbMaterials == null || dbMaterials.size() == 0 || materials.size() != dbMaterials.size()) {
                throw new CustomException(AppHttpCodeEnum.MATERIAL_REFERENCE_FAIL);
            }

            List<Integer> idList = dbMaterials.stream().map(WmMaterial::getId).collect(Collectors.toList());
            wmNewsMaterialMapper.saveRelations(idList,newsId,type);
        }

    }

    private void saveRelativeInfoForCover(WmNewsDto dto, WmNews wmNews, List<String> materials){
        List<String> images = dto.getImages();
        if(dto.getType().equals(WemediaConstants.WM_NEWS_TYPE_AUTO)){
            if(materials.size() >=3){
                wmNews.setType(WemediaConstants.WM_NEWS_MANY_IMAGE);
                images = materials.stream().limit(3).collect(Collectors.toList());
            }else if(dto.getType().equals(WemediaConstants.WM_NEWS_NONE_IMAGE)){
                wmNews.setType(WemediaConstants.WM_NEWS_NONE_IMAGE);
            }else {
                wmNews.setType(WemediaConstants.WM_NEWS_SINGLE_IMAGE);
                images = materials.stream().limit(1).collect(Collectors.toList());
            }
            if(images !=null && images.size() !=0){
                wmNews.setImages(StringUtils.join(images,","));
            }
            updateById(wmNews);
        }
        if(images !=null && images.size() !=0){
            saveRelativeInfo(images,wmNews.getId(),WemediaConstants.WM_COVER_REFERENCE);
        }


    }


}
