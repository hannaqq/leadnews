package com.news.behavior.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.news.behavior.mapper.ApShowBehaviorMapper;
import com.news.behavior.service.ApShowBehaviorService;
import com.news.model.behavior.pojos.ApShowBehavior;
import org.springframework.stereotype.Service;

@Service
public class ApShowBehaviorServiceImpl extends ServiceImpl<ApShowBehaviorMapper, ApShowBehavior> implements ApShowBehaviorService {
}
