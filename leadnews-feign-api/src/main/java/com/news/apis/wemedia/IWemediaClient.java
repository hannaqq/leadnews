package com.news.apis.wemedia;


import com.news.model.wemedia.dtos.CreatorAccountProvisionDto;
import com.news.model.wemedia.vos.CreatorAccountVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "leadnews-wemedia")
public interface IWemediaClient {

    @GetMapping("/internal/api/v1/creator-accounts/by-app-user/{id}")
    CreatorAccountVo getByUserId(@PathVariable Integer id);

    @GetMapping("/internal/api/v1/creator-accounts/{id}")
    CreatorAccountVo getById(@PathVariable Integer id);

    @PostMapping("/internal/api/v1/creator-accounts")
    CreatorAccountVo provisionCreatorAccount(@RequestBody CreatorAccountProvisionDto dto);
}
