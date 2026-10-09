package com.news.wemedia.service.impl;

import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.wemedia.dtos.WmLoginDto;
import com.news.model.wemedia.pojos.WmUser;
import com.news.utils.common.AppJwtUtil;
import com.news.wemedia.repository.WmUserRepository;
import com.news.wemedia.service.WmUserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
@RequiredArgsConstructor
public class WmUserServiceImpl implements WmUserService {
    private final WmUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ResponseResult login(WmLoginDto dto) {
        if (StringUtils.isBlank(dto.getName()) || StringUtils.isBlank(dto.getPassword())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "please insert username and password");
        }
        WmUser user = repository.findByName(dto.getName()).orElse(null);
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
        }
        HashMap<String, Object> data = new HashMap<>();
        data.put("token", AppJwtUtil.getToken(user.getId().longValue(), "wemedia"));
        user.setPassword("");
        data.put("user", user);
        return ResponseResult.okResult(data);
    }

    @Override
    public WmUser findByApUserId(Integer apUserId) {
        return repository.findByApUserId(apUserId).orElse(null);
    }

    @Override
    public WmUser findById(Integer id) {
        return repository.findById(id).orElse(null);
    }

    @Override
    public WmUser save(WmUser user) {
        if (user.getApUserId() == null) {
            return repository.save(user);
        }
        WmUser existing = repository.findByApUserId(user.getApUserId()).orElse(null);
        if (existing != null) {
            return existing;
        }
        user.setId(null);
        try {
            return repository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            return repository.findByApUserId(user.getApUserId())
                    .orElseThrow(() -> exception);
        }
    }
}
