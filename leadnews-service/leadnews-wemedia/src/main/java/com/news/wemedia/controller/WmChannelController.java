package com.news.wemedia.controller;

import com.news.model.wemedia.dtos.ChannelDto;
import lombok.RequiredArgsConstructor;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.wemedia.pojos.WmChannel;
import com.news.wemedia.service.WmChannelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/channel")
@RequiredArgsConstructor
public class WmChannelController {

    private final WmChannelService wmChannelService;

    @GetMapping("/channels")
    public ResponseResult getAll(){
        return wmChannelService.findAll();
    }

    @PostMapping("/list")
    public ResponseResult getList(@RequestBody ChannelDto dto){
        return wmChannelService.getList(dto);
    }

    @PostMapping("/save")
    public ResponseResult save(@RequestBody WmChannel wmChannel){
        return wmChannelService.saveOrUpdateChannel(wmChannel);
    }

    @PostMapping("/update")
    public ResponseResult update(@RequestBody WmChannel wmChannel){
        return wmChannelService.saveOrUpdateChannel(wmChannel);
    }

    @GetMapping("/del/{id}")
    public ResponseResult delete(@PathVariable Integer id){
        return wmChannelService.delete(id);
    }
}
