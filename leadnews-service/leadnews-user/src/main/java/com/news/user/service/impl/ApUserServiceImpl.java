package com.news.user.service.impl;

import com.news.user.repository.ApUserRepository;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.user.dtos.LoginDto;
import com.news.model.user.pojos.ApUser;
import com.news.user.service.ApUserService;
import com.news.utils.common.AppJwtUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ApUserServiceImpl implements ApUserService {

    private final ApUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public ResponseResult login(LoginDto dto) {
        if (StringUtils.isNotBlank(dto.getPhone()) && StringUtils.isNotBlank(dto.getPassword())) {
            ApUser dbUser = repository.findByPhone(dto.getPhone()).orElse(null);
            if (dbUser == null) {
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST, "user doesn't exist");
            }
            if (!passwordEncoder.matches(dto.getPassword(), dbUser.getPassword())) {
                return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
            }

            String token = AppJwtUtil.getToken(dbUser.getId().longValue());
            Map<String, Object> map = new HashMap<>();
            map.put("token", token);
            dbUser.setPassword("");
            map.put("user",dbUser);
            return ResponseResult.okResult(map);
        } else {
            Map<String, Object> map = new HashMap<>();
            map.put("token", AppJwtUtil.getToken(0L));
            return ResponseResult.okResult(map);
        }
    }
}
