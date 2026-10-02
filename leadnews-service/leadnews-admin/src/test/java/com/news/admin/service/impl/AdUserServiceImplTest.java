package com.news.admin.service.impl;

import com.news.admin.repository.AdUserRepository;
import com.news.model.admin.dtos.AdUserDto;
import com.news.model.admin.pojos.AdUser;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import org.junit.jupiter.api.Test;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AdUserServiceImplTest {

    private final AdUserRepository repository = mock(AdUserRepository.class);
    private final AdUserServiceImpl service = new AdUserServiceImpl(repository);

    @Test
    void returnsSanitizedUserAndTokenForValidCredentials() {
        AdUserDto dto = credentials("admin", "secret");
        AdUser user = user("admin", "secret", "salt");
        when(repository.findByName("admin")).thenReturn(Optional.of(user));

        ResponseResult<?> response = service.login(dto);

        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), response.getCode());
        Map<?, ?> data = (Map<?, ?>) response.getData();
        assertNotNull(data.get("token"));
        AdUser returnedUser = (AdUser) data.get("user");
        assertEquals("", returnedUser.getPassword());
        assertEquals("", returnedUser.getSalt());
        verify(repository).findByName("admin");
    }

    @Test
    void rejectsInvalidPassword() {
        AdUserDto dto = credentials("admin", "wrong");
        when(repository.findByName("admin"))
                .thenReturn(Optional.of(user("admin", "secret", "salt")));

        ResponseResult<?> response = service.login(dto);

        assertEquals(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR.getCode(), response.getCode());
    }

    @Test
    void keepsAnonymousLoginBehaviorForBlankCredentials() {
        ResponseResult<?> response = service.login(new AdUserDto());

        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), response.getCode());
        assertNotNull(((Map<?, ?>) response.getData()).get("token"));
        verifyNoInteractions(repository);
    }

    private static AdUserDto credentials(String name, String password) {
        AdUserDto dto = new AdUserDto();
        dto.setName(name);
        dto.setPassword(password);
        return dto;
    }

    private static AdUser user(String name, String password, String salt) {
        AdUser user = new AdUser();
        user.setId(1);
        user.setName(name);
        user.setSalt(salt);
        user.setPassword(DigestUtils.md5DigestAsHex(
                (password + salt).getBytes(StandardCharsets.UTF_8)));
        return user;
    }
}
