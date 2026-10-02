package com.news.article.service.impl;

import com.news.article.repository.ApAuthorRepository;
import com.news.model.article.pojos.ApAuthor;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApAuthorServiceImplTest {

    private final ApAuthorRepository repository = mock(ApAuthorRepository.class);
    private final ApAuthorServiceImpl service = new ApAuthorServiceImpl(repository);

    @Test
    void returnsExistingAuthorWithoutInserting() {
        ApAuthor request = author(10);
        ApAuthor existing = author(10);
        existing.setId(1);
        when(repository.findByUserId(10)).thenReturn(Optional.of(existing));

        ApAuthor result = service.save(request);

        assertSame(existing, result);
        verify(repository).findByUserId(10);
    }

    @Test
    void returnsConcurrentWinnerAfterUniqueConstraintConflict() {
        ApAuthor request = author(10);
        ApAuthor winner = author(10);
        winner.setId(1);
        when(repository.findByUserId(10))
                .thenReturn(Optional.empty(), Optional.of(winner));
        when(repository.saveAndFlush(request))
                .thenThrow(new DataIntegrityViolationException("duplicate user_id"));

        ApAuthor result = service.save(request);

        assertSame(winner, result);
        verify(repository).saveAndFlush(request);
    }

    private static ApAuthor author(int userId) {
        ApAuthor author = new ApAuthor();
        author.setUserId(userId);
        return author;
    }
}
