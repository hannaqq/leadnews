package com.news.wemedia.feign;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.news.apis.wemedia.IWemediaClient;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.wemedia.pojos.WmUser;
import com.news.wemedia.service.WmUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class WemediaClient implements IWemediaClient {

    @Autowired
    private WmUserService wmUserService;

    @GetMapping("/getOne/{id}")
    public WmUser getByUserId(@PathVariable Integer id){
        return wmUserService.getOne(Wrappers.<WmUser>lambdaQuery().eq(WmUser::getApUserId,id));
    };

    @PostMapping("/save")
    public ResponseResult saveWmUser(@RequestBody WmUser wmUser){
        wmUserService.save(wmUser);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    };
}
