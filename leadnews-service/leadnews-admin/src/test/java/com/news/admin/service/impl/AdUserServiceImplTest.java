package com.news.admin.service.impl;

import com.news.admin.repository.AdUserRepository;
import com.news.model.admin.dtos.AdUserDto;
import com.news.model.admin.pojos.AdUser;
import com.news.model.common.dtos.ResponseResult;
import com.news.model.common.enums.AppHttpCodeEnum;
import com.news.utils.common.AppJwtUtil;
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

class AdUserServiceImplTest {

    private final AdUserRepository repository = mock(AdUserRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private final AdUserServiceImpl service = new AdUserServiceImpl(repository, passwordEncoder);

    @Test
    void returnsSanitizedUserAndTokenForValidCredentials() {
        AdUserDto dto = credentials("admin", "secret");
        AdUser user = user("admin", "secret");
        when(repository.findByName("admin")).thenReturn(Optional.of(user));

        ResponseResult<?> response = service.login(dto);

        assertEquals(AppHttpCodeEnum.SUCCESS.getCode(), response.getCode());
        Map<?, ?> data = (Map<?, ?>) response.getData();
        assertNotNull(data.get("token"));
        assertEquals("admin", AppJwtUtil.getClaimsBody((String) data.get("token")).getAudience());
        AdUser returnedUser = (AdUser) data.get("user");
        assertEquals("", returnedUser.getPassword());
        verify(repository).findByName("admin");
    }

    @Test
    void rejectsInvalidPassword() {
        AdUserDto dto = credentials("admin", "wrong");
        when(repository.findByName("admin"))
                .thenReturn(Optional.of(user("admin", "secret")));

        ResponseResult<?> response = service.login(dto);

        assertEquals(AppHttpCodeEnum.LOGIN_PASSWORD_ERROR.getCode(), response.getCode());
    }

    @Test
    void rejectsBlankCredentials() {
        ResponseResult<?> response = service.login(new AdUserDto());

        assertEquals(AppHttpCodeEnum.PARAM_INVALID.getCode(), response.getCode());
        verifyNoInteractions(repository);
    }

    private static AdUserDto credentials(String name, String password) {
        AdUserDto dto = new AdUserDto();
        dto.setName(name);
        dto.setPassword(password);
        return dto;
    }

    private AdUser user(String name, String password) {
        AdUser user = new AdUser();
        user.setId(1);
        user.setName(name);
        user.setPassword(passwordEncoder.encode(password));
        return user;
    }
}
