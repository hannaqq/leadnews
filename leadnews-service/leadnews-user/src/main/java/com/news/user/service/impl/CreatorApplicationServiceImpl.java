package com.news.user.service.impl;

import com.news.apis.wemedia.IWemediaClient;
import com.news.model.common.dtos.PageResponseResult;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.user.dtos.CreatorApplicationQueryDto;
import com.news.model.user.dtos.CreatorApplicationRejectDto;
import com.news.model.user.dtos.CreatorApplicationSubmitDto;
import com.news.model.user.enums.CreatorApplicationStatus;
import com.news.model.user.pojos.ApUser;
import com.news.model.user.pojos.CreatorApplication;
import com.news.model.user.vos.CreatorApplicationVo;
import com.news.model.wemedia.dtos.CreatorAccountProvisionDto;
import com.news.model.wemedia.vos.CreatorAccountVo;
import com.news.user.repository.ApUserRepository;
import com.news.user.repository.CreatorApplicationRepository;
import com.news.user.service.CreatorApplicationService;
import com.news.utils.thread.AppThreadLocalUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class CreatorApplicationServiceImpl implements CreatorApplicationService {

    private final CreatorApplicationRepository applicationRepository;
    private final ApUserRepository userRepository;
    private final IWemediaClient wemediaClient;

    @Override
    public ResponseResult submit(CreatorApplicationSubmitDto dto) {
        Integer currentUserId = AppThreadLocalUtil.getUserId();
        if (currentUserId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        CreatorApplication existing = applicationRepository.findByUserId(currentUserId).orElse(null);
        if (existing == null) {
            CreatorApplication application = new CreatorApplication();
            application.setUserId(currentUserId);
            application.setDisplayName(dto.getDisplayName());
            application.setBio(dto.getBio());
            application.setCategory(dto.getCategory());
            application.setPortfolioUrl(dto.getPortfolioUrl());
            application.setStatus(CreatorApplicationStatus.PENDING);
            Date now = new Date();
            application.setCreatedTime(now);
            application.setSubmittedTime(now);
            application.setUpdatedTime(now);
            try {
                applicationRepository.saveAndFlush(application);
                return ResponseResult.okResult(toVo(application));
            } catch (DataIntegrityViolationException exception) {
                return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST, "creator application already exists");
            }
        }

        if (existing.getStatus() == CreatorApplicationStatus.REJECTED
                && applicationRepository.resubmit(
                        existing.getId(),
                        CreatorApplicationStatus.REJECTED,
                        CreatorApplicationStatus.PENDING,
                        dto.getDisplayName(),
                        dto.getBio(),
                        dto.getCategory(),
                        dto.getPortfolioUrl(),
                        new Date()) == 1) {
            return ResponseResult.okResult(toVo(
                    applicationRepository.findById(existing.getId()).orElseThrow()));
        }
        if (existing.getStatus() == CreatorApplicationStatus.APPROVED) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST, "user is already a creator");
        }
        return ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST, "creator application is pending review");
    }

    @Override
    public ResponseResult getMine() {
        Integer currentUserId = AppThreadLocalUtil.getUserId();
        if (currentUserId == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }
        CreatorApplication application = applicationRepository.findByUserId(currentUserId).orElse(null);
        return application == null
                ? ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST)
                : ResponseResult.okResult(toVo(application));
    }

    @Override
    public ResponseResult getList(CreatorApplicationQueryDto dto) {
        dto.checkParam();
        Page<CreatorApplication> page = applicationRepository.findForReview(
                dto.getId(),
                dto.getStatus(),
                PageRequest.of(dto.getPage() - 1, dto.getSize(),
                        Sort.by(Sort.Direction.DESC, "submittedTime")));
        PageResponseResult response = new PageResponseResult(
                dto.getPage(), dto.getSize(), Math.toIntExact(page.getTotalElements()));
        response.setData(page.getContent().stream().map(this::toVo).toList());
        return response;
    }

    @Override
    public ResponseResult approve(Integer id) {
        CreatorApplication application = applicationRepository.findById(id).orElse(null);
        if (application == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.DATA_NOT_EXIST);
        }
        if (application.getStatus() == CreatorApplicationStatus.APPROVED) {
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }
        if (application.getStatus() == CreatorApplicationStatus.REJECTED) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID, "rejected application must be resubmitted");
        }
        ApUser user = userRepository.findById(application.getUserId()).orElse(null);
        if (user == null) {
            return ResponseResult.errorResult(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST);
        }

        try {
            CreatorAccountVo creatorAccount = wemediaClient.getByUserId(user.getId());
            if (creatorAccount == null) {
                creatorAccount = wemediaClient.provisionCreatorAccount(
                        toProvisionDto(user, application));
            }
            if (creatorAccount == null || creatorAccount.getId() == null) {
                return ResponseResult.errorResult(
                        AppHttpCodeEnum.SERVER_ERROR,
                        "creator account provisioning result is unknown; retry approval");
            }
        } catch (RuntimeException exception) {
            log.warn("Creator account provisioning is unresolved for application {}", id, exception);
            return ResponseResult.errorResult(
                    AppHttpCodeEnum.SERVER_ERROR,
                    "creator account provisioning result is unknown; retry approval");
        }

        if (applicationRepository.completeReview(
                id,
                CreatorApplicationStatus.PENDING,
                CreatorApplicationStatus.APPROVED,
                new Date()) == 1) {
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }
        CreatorApplication current = applicationRepository.findById(id).orElseThrow();
        return current.getStatus() == CreatorApplicationStatus.APPROVED
                ? ResponseResult.okResult(AppHttpCodeEnum.SUCCESS)
                : ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR, "failed to complete creator approval");
    }

    @Override
    public ResponseResult reject(Integer id, CreatorApplicationRejectDto dto) {
        return applicationRepository.reject(
                id,
                CreatorApplicationStatus.PENDING,
                CreatorApplicationStatus.REJECTED,
                dto.getReviewNote(),
                new Date()) == 1
                ? ResponseResult.okResult(AppHttpCodeEnum.SUCCESS)
                : ResponseResult.errorResult(AppHttpCodeEnum.DATA_EXIST, "only pending applications can be rejected");
    }

    private CreatorAccountProvisionDto toProvisionDto(ApUser user, CreatorApplication application) {
        CreatorAccountProvisionDto dto = new CreatorAccountProvisionDto();
        dto.setApUserId(user.getId());
        dto.setName(user.getName());
        dto.setPassword(user.getPassword());
        dto.setPhone(user.getPhone());
        dto.setImage(user.getImage());
        dto.setNickname(application.getDisplayName());
        return dto;
    }

    private CreatorApplicationVo toVo(CreatorApplication application) {
        CreatorApplicationVo vo = new CreatorApplicationVo();
        BeanUtils.copyProperties(application, vo);
        return vo;
    }

}
