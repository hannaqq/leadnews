package com.news.article.service.impl;

import com.news.article.repository.ApAuthorRepository;
import com.news.article.service.ApAuthorService;
import com.news.model.article.pojos.ApAuthor;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApAuthorServiceImpl implements ApAuthorService {

    private final ApAuthorRepository repository;

    @Override
    public ApAuthor save(ApAuthor author) {
        ApAuthor existing = repository.findByUserId(author.getUserId()).orElse(null);
        if (existing != null) {
            return existing;
        }

        // The unique constraint is the final guard when concurrent requests both
        // observe that no author exists yet.
        author.setId(null);
        try {
            return repository.saveAndFlush(author);
        } catch (DataIntegrityViolationException exception) {
            return repository.findByUserId(author.getUserId())
                    .orElseThrow(() -> exception);
        }
    }

    @Override
    public ApAuthor findByUserId(Integer userId) {
        return repository.findByUserId(userId).orElse(null);
    }
}
