package com.news.user.service;

import com.news.model.common.dtos.ResponseResult;
import com.news.model.user.dtos.CreatorApplicationQueryDto;
import com.news.model.user.dtos.CreatorApplicationRejectDto;
import com.news.model.user.dtos.CreatorApplicationSubmitDto;

public interface CreatorApplicationService {

    ResponseResult submit(CreatorApplicationSubmitDto dto);

    ResponseResult getMine();

    ResponseResult getList(CreatorApplicationQueryDto dto);

    ResponseResult approve(Integer id);

    ResponseResult reject(Integer id, CreatorApplicationRejectDto dto);
}
