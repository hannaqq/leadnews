package com.news.wemedia.feign;

import lombok.RequiredArgsConstructor;
import com.news.apis.wemedia.IWemediaClient;
import com.news.common.constants.WemediaConstants;
import com.news.model.wemedia.dtos.CreatorAccountProvisionDto;
import com.news.model.wemedia.pojos.WmUser;
import com.news.model.wemedia.vos.CreatorAccountVo;
import com.news.wemedia.service.WmUserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class WemediaClient implements IWemediaClient {

    private final WmUserService wmUserService;

    @GetMapping("/internal/api/v1/creator-accounts/by-app-user/{id}")
    public CreatorAccountVo getByUserId(@PathVariable Integer id){
        return toVo(wmUserService.findByApUserId(id));
    }

    @GetMapping("/internal/api/v1/creator-accounts/{id}")
    public CreatorAccountVo getById(@PathVariable Integer id) {
        WmUser user = wmUserService.findById(id);
        if (user == null || !WemediaConstants.WM_USER_OK.equals(user.getStatus())) {
            return null;
        }
        return toVo(user);
    }

    @PostMapping("/internal/api/v1/creator-accounts")
    public CreatorAccountVo provisionCreatorAccount(@RequestBody CreatorAccountProvisionDto dto){
        WmUser wmUser = new WmUser();
        wmUser.setApUserId(dto.getApUserId());
        wmUser.setName(dto.getName());
        wmUser.setPassword(dto.getPassword());
        wmUser.setSalt(dto.getSalt());
        wmUser.setPhone(dto.getPhone());
        wmUser.setImage(dto.getImage());
        wmUser.setNickname(dto.getNickname());
        wmUser.setStatus(WemediaConstants.WM_USER_OK);
        wmUser.setCreatedTime(new java.util.Date());
        return toVo(wmUserService.save(wmUser));
    }

    private CreatorAccountVo toVo(WmUser user) {
        return user == null ? null : new CreatorAccountVo(user.getId(), user.getApUserId());
    }
}
