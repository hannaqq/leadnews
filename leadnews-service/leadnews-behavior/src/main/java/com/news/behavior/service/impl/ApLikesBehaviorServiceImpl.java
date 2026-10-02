package com.news.behavior.service.impl;

import lombok.RequiredArgsConstructor;

import com.news.behavior.service.ApBehaviorEntryService;
import com.news.behavior.service.ApLikesBehaviorService;
import com.news.common.constants.SystemConstants;
import com.news.model.behavior.dtos.LikesBehaviorDto;
import com.news.model.behavior.pojos.ApBehaviorEntry;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.user.pojos.ApUser;
import com.news.utils.thread.AppThreadLocalUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApLikesBehaviorServiceImpl implements ApLikesBehaviorService {
    private final ApBehaviorEntryService apBehaviorEntryService;

    @Override
    public ResponseResult like(LikesBehaviorDto dto) {
        if(dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        ApUser user = AppThreadLocalUtil.getUser();
        ApBehaviorEntry entry = null;
        if(user != null){
            entry = apBehaviorEntryService.findByUserIdOrEquipmentId(user.getId(), SystemConstants.TYPE_USER);
        }
        if(entry ==null){
            throw new RuntimeException("entry doesn't exist");
        }
        if(dto.getOperation() ==1){

        }
        return null;
    }
}
