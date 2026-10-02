package com.news.wemedia.service.impl;

import com.news.model.common.dtos.PageResponseResult;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.wemedia.dtos.ChannelDto;
import com.news.model.wemedia.pojos.WmChannel;
import com.news.wemedia.repository.WmChannelRepository;
import com.news.wemedia.service.WmChannelService;
import com.news.wemedia.service.WmNewsService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class WmChannelServiceImpl implements WmChannelService {
    private final WmChannelRepository repository;
    private final WmNewsService wmNewsService;

    @Override
    public ResponseResult findAll() {
        return ResponseResult.okResult(repository.findAll());
    }

    @Override
    public ResponseResult getList(ChannelDto dto) {
        dto.checkParam();
        PageRequest request = PageRequest.of(dto.getPage() - 1, dto.getSize(),
                Sort.by(Sort.Direction.DESC, "createdTime"));
        Page<WmChannel> page = StringUtils.isBlank(dto.getName())
                ? repository.findAll(request)
                : repository.findByNameContainingIgnoreCase(dto.getName(), request);
        ResponseResult result = new PageResponseResult(dto.getPage(), dto.getSize(),
                Math.toIntExact(page.getTotalElements()));
        result.setData(page.getContent());
        return result;
    }

    @Override
    public ResponseResult saveOrUpdateChannel(WmChannel channel) {
        if (channel.getId() != null) {
            WmChannel existing = repository.findById(channel.getId()).orElseThrow();
            if (channel.getName() != null) existing.setName(channel.getName());
            if (channel.getDescription() != null) existing.setDescription(channel.getDescription());
            if (channel.getIsDefault() != null) existing.setIsDefault(channel.getIsDefault());
            if (channel.getStatus() != null) existing.setStatus(channel.getStatus());
            if (channel.getOrd() != null) existing.setOrd(channel.getOrd());
            channel = existing;
        }
        channel.setCreatedTime(new Date());
        repository.save(channel);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult delete(Integer id) {
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        if (wmNewsService.existsByChannelId(id)) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "channel is used in article");
        }
        repository.deleteById(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
