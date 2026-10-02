package com.news.behavior.service.impl;

import com.news.behavior.repository.ApBehaviorEntryRepository;
import com.news.behavior.service.ApBehaviorEntryService;
import com.news.model.behavior.pojos.ApBehaviorEntry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApBehaviorEntryImpl implements ApBehaviorEntryService {

    private final ApBehaviorEntryRepository repository;

    @Override
    public ApBehaviorEntry findByUserIdOrEquipmentId(Integer userId, Integer type) {
        return repository.findByEntryIdAndType(userId, type.shortValue()).orElse(null);
    }
}
