package com.news.wemedia.service.impl;

import com.news.model.common.dtos.PageResponseResult;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.wemedia.dtos.SensitiveDto;
import com.news.model.wemedia.pojos.WmSensitive;
import com.news.wemedia.repository.WmSensitiveRepository;
import com.news.wemedia.service.WmSensitiveService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class WmSensitiveServiceImpl implements WmSensitiveService {
    private final WmSensitiveRepository repository;

    @Override
    public ResponseResult getList(SensitiveDto dto) {
        dto.checkParam();
        PageRequest request = PageRequest.of(dto.getPage() - 1, dto.getSize(),
                Sort.by(Sort.Direction.DESC, "createdTime"));
        Page<WmSensitive> page = StringUtils.isBlank(dto.getName())
                ? repository.findAll(request)
                : repository.findBySensitivesContainingIgnoreCase(dto.getName(), request);
        ResponseResult result = new PageResponseResult(dto.getPage(), dto.getSize(),
                Math.toIntExact(page.getTotalElements()));
        result.setData(page.getContent());
        return result;
    }

    @Override
    public ResponseResult delete(Integer id) {
        if (id == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        repository.deleteById(id);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    @Override
    public ResponseResult saveOrUpdateSensitive(WmSensitive sensitive) {
        if (sensitive.getId() != null) {
            WmSensitive existing = repository.findById(sensitive.getId()).orElseThrow();
            if (sensitive.getSensitives() != null) {
                existing.setSensitives(sensitive.getSensitives());
            }
            sensitive = existing;
        }
        sensitive.setCreatedTime(new Date());
        repository.save(sensitive);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
