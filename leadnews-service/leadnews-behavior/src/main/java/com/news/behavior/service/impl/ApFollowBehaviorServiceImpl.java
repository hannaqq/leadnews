package com.news.behavior.service.impl;

import lombok.RequiredArgsConstructor;
import com.news.behavior.repository.ApFollowBehaviorRepository;
import com.news.behavior.service.ApBehaviorEntryService;
import com.news.behavior.service.ApFollowBehaviorService;
import com.news.common.constants.SystemConstants;
import com.news.model.behavior.dtos.FollowBehaviorDto;
import com.news.model.behavior.pojos.ApBehaviorEntry;
import com.news.model.behavior.pojos.ApFollowBehavior;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class ApFollowBehaviorServiceImpl implements ApFollowBehaviorService {

    private final ApBehaviorEntryService apBehaviorEntryService;
    private final ApFollowBehaviorRepository repository;


    @Override
    public void saveFollowBehavior(FollowBehaviorDto dto) {
        ApBehaviorEntry entry = apBehaviorEntryService.findByUserIdOrEquipmentId(dto.getUserId(), SystemConstants.TYPE_USER);
        if(entry != null){
            ApFollowBehavior apFollowBehavior = new ApFollowBehavior();
            apFollowBehavior.setFollowId(dto.getFollowId());
            apFollowBehavior.setArticleId(dto.getArticleId());
            apFollowBehavior.setEntryId(entry.getEntryId());
            apFollowBehavior.setCreatedTime(new Date());
            repository.save(apFollowBehavior);
        }
    }
}
