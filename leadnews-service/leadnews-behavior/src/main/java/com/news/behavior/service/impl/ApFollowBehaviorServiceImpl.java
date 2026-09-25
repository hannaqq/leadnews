package com.news.behavior.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import com.news.behavior.mapper.ApFollowBehaviorMapper;
import com.news.behavior.service.ApBehaviorEntryService;
import com.news.behavior.service.ApFollowBehaviorService;
import com.news.common.constants.SystemConstants;
import com.news.model.behavior.dtos.FollowBehaviorDto;
import com.news.model.behavior.pojos.ApBehaviorEntry;
import com.news.model.behavior.pojos.ApFollowBehavior;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class ApFollowBehaviorServiceImpl extends ServiceImpl<ApFollowBehaviorMapper, ApFollowBehavior> implements ApFollowBehaviorService {

    private final ApBehaviorEntryService apBehaviorEntryService;


    @Override
    public void saveFollowBehavior(FollowBehaviorDto dto) {
        ApBehaviorEntry entry = apBehaviorEntryService.findByUserIdOrEquipmentId(dto.getUserId(), SystemConstants.TYPE_USER);
        if(entry != null){
            ApFollowBehavior apFollowBehavior = new ApFollowBehavior();
            apFollowBehavior.setFollowId(dto.getFollowId());
            apFollowBehavior.setArticleId(dto.getArticleId());
            apFollowBehavior.setEntryId(entry.getEntryId());
            apFollowBehavior.setCreatedTime(new Date());
            save(apFollowBehavior);
        }
    }
}
