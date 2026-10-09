package com.news.wemedia.service;

import com.news.model.wemedia.pojos.WmUser;
import com.news.wemedia.repository.WmUserRepository;
import com.news.wemedia.service.impl.WmUserServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WmUserServiceImplTest {
    private final WmUserRepository repository = mock(WmUserRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(4);
    private final WmUserServiceImpl service = new WmUserServiceImpl(repository, passwordEncoder);

    @Test
    void acceptsBcryptPassword() {
        WmUser user = user(10);
        user.setId(1);
        user.setName("creator");
        user.setPassword(passwordEncoder.encode("secret"));
        when(repository.findByName("creator")).thenReturn(Optional.of(user));

        var credentials = new com.news.model.wemedia.dtos.WmLoginDto();
        credentials.setName("creator");
        credentials.setPassword("secret");

        assertEquals(200, service.login(credentials).getCode());
    }

    @Test
    void returnsExistingAccountWithoutInserting() {
        WmUser request = user(10);
        WmUser existing = user(10);
        existing.setId(1);
        when(repository.findByApUserId(10)).thenReturn(Optional.of(existing));

        assertSame(existing, service.save(request));
        verify(repository).findByApUserId(10);
    }

    @Test
    void returnsConcurrentWinnerAfterUniqueConstraintConflict() {
        WmUser request = user(10);
        WmUser winner = user(10);
        winner.setId(1);
        when(repository.findByApUserId(10)).thenReturn(Optional.empty(), Optional.of(winner));
        when(repository.saveAndFlush(request))
                .thenThrow(new DataIntegrityViolationException("duplicate ap_user_id"));

        assertSame(winner, service.save(request));
    }

    private static WmUser user(int apUserId) {
        WmUser user = new WmUser();
        user.setApUserId(apUserId);
        return user;
    }
}
