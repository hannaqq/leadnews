package com.news.behavior.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.news.behavior.mapper.ApReadBehaviorMapper;
import com.news.behavior.service.ApReadBehaviorService;
import com.news.model.behavior.pojos.ApReadBehavior;
import org.springframework.stereotype.Service;

@Service
public class ApReadBehaviorServiceImpl extends ServiceImpl<ApReadBehaviorMapper, ApReadBehavior> implements ApReadBehaviorService {
}
