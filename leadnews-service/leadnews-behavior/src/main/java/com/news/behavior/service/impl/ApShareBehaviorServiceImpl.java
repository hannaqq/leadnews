package com.news.behavior.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.news.behavior.mapper.ApShareBehaviorMapper;
import com.news.behavior.service.ApShareBehaviorService;
import com.news.model.behavior.pojos.ApShareBehavior;
import org.springframework.stereotype.Service;

@Service
public class ApShareBehaviorServiceImpl extends ServiceImpl<ApShareBehaviorMapper, ApShareBehavior> implements ApShareBehaviorService {
}
