package com.news.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.wemedia.dtos.WmLoginDto;
import com.news.model.wemedia.pojos.WmUser;
import com.news.utils.common.AppJwtUtil;
import com.news.wemedia.mapper.WmUserMapper;
import com.news.wemedia.service.WmUserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;

@Service
@Slf4j
public class WmUserServiceImpl extends ServiceImpl<WmUserMapper, WmUser> implements WmUserService {
    @Override
    public ResponseResult login(WmLoginDto dto) {
        if(StringUtils.isBlank(dto.getName()) || StringUtils.isBlank(dto.getPassword())){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID,"please insert username and password");
        }

        LambdaQueryWrapper<WmUser> lqw = new LambdaQueryWrapper<>();
        WmUser wmUser = getOne(lqw.eq(WmUser::getName, dto.getName()));
        if(wmUser == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }

        String salt = wmUser.getSalt();
        String password = dto.getPassword();
        password = DigestUtils.md5DigestAsHex((password + salt).getBytes());
        if(!password.equals(wmUser.getPassword())){
            return ResponseResult.errorResult(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR);
        }
        String token = AppJwtUtil.getToken(wmUser.getId().longValue());
        HashMap<String, Object> map = new HashMap<>();
        map.put("token",token);
        wmUser.setSalt("");
        wmUser.setPassword("");
        map.put("user",wmUser);
        return ResponseResult.okResult(map);

    }
}
