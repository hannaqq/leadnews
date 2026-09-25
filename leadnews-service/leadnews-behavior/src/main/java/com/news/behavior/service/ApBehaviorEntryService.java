package com.news.behavior.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.news.model.behavior.pojos.ApBehaviorEntry;

public interface ApBehaviorEntryService extends IService<ApBehaviorEntry> {
    public ApBehaviorEntry findByUserIdOrEquipmentId(Integer userId, Integer type);
}
