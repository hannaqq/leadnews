package com.news.behavior.service;

import com.news.model.behavior.pojos.ApBehaviorEntry;

public interface ApBehaviorEntryService {
    ApBehaviorEntry findByUserIdOrEquipmentId(Integer userId, Integer type);
}
