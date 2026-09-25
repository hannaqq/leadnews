package com.news.wemedia.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.wemedia.dtos.NewsAuthDto;
import com.news.model.wemedia.dtos.WmNewsDto;
import com.news.model.wemedia.dtos.WmNewsPageReqDto;
import com.news.model.wemedia.pojos.WmNews;

public interface WmNewsService extends IService<WmNews> {
    public ResponseResult getList(WmNewsPageReqDto dto);

    public ResponseResult submit(WmNewsDto dto);

    public ResponseResult downOrUp(WmNewsDto dto);

    public ResponseResult getList_vo(NewsAuthDto dto);

    public ResponseResult getDetail(Integer id);

    public ResponseResult authFail(NewsAuthDto dto);

    public ResponseResult authPass(NewsAuthDto dto);

    public ResponseResult delNews(Integer id);

    public ResponseResult getOne(Integer id);

}
