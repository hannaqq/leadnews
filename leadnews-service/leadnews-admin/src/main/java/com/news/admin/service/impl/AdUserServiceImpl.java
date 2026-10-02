package com.news.admin.service.impl;


import com.news.admin.repository.AdUserRepository;
import com.news.admin.service.AdUserService;
import com.news.model.admin.dtos.AdUserDto;
import com.news.model.admin.pojos.AdUser;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.utils.common.AppJwtUtil;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdUserServiceImpl implements AdUserService {

    private final AdUserRepository adUserRepository;

    @Override
    public ResponseResult login(AdUserDto dto) {
        if(StringUtils.isBlank(dto.getName()) || StringUtils.isBlank(dto.getPassword())){
            Map<String,Object> map = new HashMap<>();
            map.put("token", AppJwtUtil.getToken(0L));
            return ResponseResult.okResult(map);
        }
        AdUser adUser = adUserRepository.findByName(dto.getName()).orElse(null);
        if(adUser == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        String salt = adUser.getSalt();
        String password = dto.getPassword();
        String pwd = DigestUtils.md5DigestAsHex((password + salt).getBytes());
        if(!pwd.equals(adUser.getPassword())){
            return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
        }
        String token = AppJwtUtil.getToken(adUser.getId().longValue());
        Map<String,Object> map = new HashMap<>();
        map.put("token", token);
        adUser.setPassword("");
        adUser.setSalt("");
        map.put("user", adUser);
        return ResponseResult.okResult(map);
    }
}
