package com.news.admin.repository;

import com.news.model.admin.pojos.AdUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest(properties = "spring.cloud.consul.enabled=false")
class AdUserRepositoryTest {

    @Autowired
    private AdUserRepository repository;

    @Test
    void findsUserByName() {
        AdUser user = new AdUser();
        user.setName("admin");
        user.setPassword("hash");
        repository.saveAndFlush(user);

        AdUser result = repository.findByName("admin").orElseThrow();

        assertTrue(result.getId() > 0);
        assertEquals("hash", result.getPassword());
    }
}
