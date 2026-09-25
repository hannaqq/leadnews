package com.news.apis.wemedia;


import com.news.model.common.dtos.ResponseResult;
import com.news.model.wemedia.pojos.WmUser;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "leadnews-wemedia")
public interface IWemediaClient {

    @GetMapping("/getOne/{id}")
    public WmUser getByUserId(@PathVariable Integer id);

    @PostMapping("/save")
    public ResponseResult saveWmUser(@RequestBody WmUser wmUser);





}
