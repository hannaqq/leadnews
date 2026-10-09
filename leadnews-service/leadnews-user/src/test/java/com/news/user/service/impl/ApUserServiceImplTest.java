package com.news.user.service.impl;

import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.model.user.dtos.LoginDto;
import com.news.model.user.pojos.ApUser;
import com.news.user.repository.ApUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ApUserServiceImplTest {

    private final ApUserRepository repository = mock(ApUserRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private final ApUserServiceImpl service = new ApUserServiceImpl(repository, passwordEncoder);

    @Test
    void returnsSanitizedUserAndTokenForValidCredentials() {
        LoginDto dto = credentials("13000000000", "secret");
        ApUser user = user("13000000000", "secret");
        when(repository.findByPhone(dto.getPhone())).thenReturn(Optional.of(user));

        ResponseResult<?> response = service.login(dto);

        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), response.getCode());
        Map<?, ?> data = (Map<?, ?>) response.getData();
        assertNotNull(data.get("token"));
        ApUser returnedUser = (ApUser) data.get("user");
        assertEquals("", returnedUser.getPassword());
        verify(repository).findByPhone(dto.getPhone());
    }

    @Test
    void rejectsInvalidPassword() {
        LoginDto dto = credentials("13000000000", "wrong");
        when(repository.findByPhone(dto.getPhone()))
                .thenReturn(Optional.of(user(dto.getPhone(), "secret")));

        ResponseResult<?> response = service.login(dto);

        assertEquals(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR.getCode(), response.getCode());
    }

    @Test
    void keepsAnonymousLoginBehaviorForBlankCredentials() {
        ResponseResult<?> response = service.login(new LoginDto());

        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), response.getCode());
        assertNotNull(((Map<?, ?>) response.getData()).get("token"));
        verifyNoInteractions(repository);
    }

    private static LoginDto credentials(String phone, String password) {
        LoginDto dto = new LoginDto();
        dto.setPhone(phone);
        dto.setPassword(password);
        return dto;
    }

    private ApUser user(String phone, String password) {
        ApUser user = new ApUser();
        user.setId(1);
        user.setPhone(phone);
        user.setPassword(passwordEncoder.encode(password));
        return user;
    }
}
