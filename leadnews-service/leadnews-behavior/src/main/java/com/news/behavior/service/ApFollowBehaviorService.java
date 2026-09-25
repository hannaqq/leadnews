package com.news.behavior.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.news.model.behavior.dtos.FollowBehaviorDto;
import com.news.model.behavior.pojos.ApFollowBehavior;

public interface ApFollowBehaviorService extends IService<ApFollowBehavior> {
    void saveFollowBehavior(FollowBehaviorDto dto);
}
