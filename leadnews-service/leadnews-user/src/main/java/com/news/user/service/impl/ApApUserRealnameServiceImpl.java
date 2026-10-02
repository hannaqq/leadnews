package com.news.user.service.impl;
import lombok.RequiredArgsConstructor;
import com.news.apis.article.IArticleClient;
import com.news.apis.wemedia.IWemediaClient;
import com.news.common.constants.ApUserConstants;
import com.news.common.constants.WemediaConstants;
import com.news.model.article.pojos.ApAuthor;
import com.news.model.common.dtos.PageResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.user.dtos.AuthDto;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.user.pojos.ApUser;
import com.news.model.user.pojos.ApUserRealname;
import com.news.model.wemedia.pojos.WmUser;
import com.news.user.repository.ApUserRealnameRepository;
import com.news.user.repository.ApUserRepository;
import com.news.user.service.ApUserRealnameService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.Date;

@Service
@Slf4j
@RequiredArgsConstructor
public class ApApUserRealnameServiceImpl implements ApUserRealnameService {

    private final ApUserRealnameRepository realnameRepository;

    private final ApUserRepository userRepository;

    private final IWemediaClient iWemediaClient;

    private final IArticleClient iArticleClient;

    @Override
    public ResponseResult getList(AuthDto dto) {
        dto.checkParam();
        Short status = dto.getStatus() == null ? null : dto.getStatus().shortValue();
        Page<ApUserRealname> page = realnameRepository.findForReview(
                dto.getId(),
                status,
                PageRequest.of(dto.getPage() - 1, dto.getSize(),
                        Sort.by(Sort.Direction.DESC, "createdTime")));
        ResponseResult responseResult = new PageResponseResult(
                dto.getPage(), dto.getSize(), Math.toIntExact(page.getTotalElements()));
        responseResult.setData(page.getContent());
        return responseResult;
    }

    @Override
    public ResponseResult pass(AuthDto dto) {
        ApUserRealname apUserRealname = realnameRepository.findById(dto.getId()).orElseThrow();
        ApUser apUser = userRepository.findById(apUserRealname.getUserId()).orElseThrow();
        WmUser wmUser = iWemediaClient.getByUserId(apUser.getId());
        if(wmUser == null){
            wmUser = new WmUser();
            BeanUtils.copyProperties(apUser, wmUser);
            wmUser.setApUserId(apUser.getId());
            wmUser.setStatus(WemediaConstants.WM_USER_OK);
            wmUser.setCreatedTime(new Date());
            ResponseResult responseResult = iWemediaClient.saveWmUser(wmUser);
            if (!isSuccess(responseResult)) {
                return ResponseResult.errorResult(
                        AppHttpCodeEnum.SERVER_ERROR, "failed to create wemedia account");
            }
            wmUser = iWemediaClient.getByUserId(apUser.getId());
            if (wmUser == null || wmUser.getId() == null) {
                return ResponseResult.errorResult(
                        AppHttpCodeEnum.SERVER_ERROR, "created wemedia account could not be loaded");
            }
            log.info("wmUser account created successfully");
        }

        ApAuthor apAuthor = iArticleClient.getByUserId(apUser.getId());
        if(apAuthor == null){
            apAuthor = new ApAuthor();
            apAuthor.setUserId(apUser.getId());
            apAuthor.setName(apUser.getName());
            apAuthor.setType((short)2);
            apAuthor.setCreatedTime(new Date());
            apAuthor.setWmUserId(wmUser.getId());
            ResponseResult responseResult = iArticleClient.saveApAuthor(apAuthor);
            if (!isSuccess(responseResult)) {
                return ResponseResult.errorResult(
                        AppHttpCodeEnum.SERVER_ERROR, "failed to create article author");
            }
        }

        apUserRealname.setStatus(ApUserConstants.AUTHORIZATION_PASS);
        apUserRealname.setUpdatedTime(new Date());
        realnameRepository.save(apUserRealname);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    private boolean isSuccess(ResponseResult responseResult) {
        return responseResult != null
                && Integer.valueOf(AppHttpCodeEnum.SUCCESS.getCode()).equals(responseResult.getCode());
    }

    @Override
    public ResponseResult fail(AuthDto dto) {
        ApUserRealname apUserRealname = realnameRepository.findById(dto.getId()).orElseThrow();
        apUserRealname.setReason(dto.getMsg());
        apUserRealname.setUpdatedTime(new Date());
        apUserRealname.setStatus(ApUserConstants.AUTHORIZATION_FAILED);
        realnameRepository.save(apUserRealname);
        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }
}
