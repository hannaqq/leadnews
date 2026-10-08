package com.news.article.service.transaction;

import com.news.article.repository.ApArticleConfigRepository;
import com.news.article.repository.ApArticleContentRepository;
import com.news.article.repository.ApArticleRepository;
import com.news.model.article.dtos.ArticleDto;
import com.news.model.article.pojos.ApArticle;
import com.news.model.article.pojos.ApArticleConfig;
import com.news.model.article.pojos.ApArticleContent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class ArticleTransactionService {

    private final ApArticleRepository articleRepository;
    private final ApArticleConfigRepository configRepository;
    private final ApArticleContentRepository contentRepository;

    @Transactional
    public ApArticle save(ArticleDto dto) {
        if (dto.getId() == null && dto.getSourceNewsId() != null) {
            articleRepository.findBySourceNewsId(dto.getSourceNewsId())
                    .ifPresent(existing -> dto.setId(existing.getId()));
        }
        if (dto.getId() == null) {
            ApArticle article = new ApArticle();
            BeanUtils.copyProperties(dto, article, "content");
            articleRepository.save(article);

            configRepository.save(new ApArticleConfig(article.getId()));
            ApArticleContent content = new ApArticleContent();
            content.setArticleId(article.getId());
            content.setContent(dto.getContent());
            contentRepository.save(content);
            return article;
        }

        ApArticle article = articleRepository.findById(dto.getId()).orElseThrow();
        BeanUtils.copyProperties(dto, article, ignoredProperties(dto));
        articleRepository.save(article);
        ApArticleContent content = contentRepository.findByArticleId(article.getId()).orElseThrow();
        if (dto.getContent() != null) {
            content.setContent(dto.getContent());
            contentRepository.save(content);
        }
        return article;
    }

    @Transactional
    public void delete(Long articleId) {
        articleRepository.deleteById(articleId);
        ApArticleConfig config = configRepository.findByArticleId(articleId).orElseThrow();
        config.setIsDelete(true);
        configRepository.save(config);
        contentRepository.deleteByArticleId(articleId);
    }

    private String[] ignoredProperties(ArticleDto dto) {
        BeanWrapper wrapper = new BeanWrapperImpl(dto);
        return Stream.concat(
                        Arrays.stream(wrapper.getPropertyDescriptors())
                                .map(PropertyDescriptor::getName)
                                .filter(name -> wrapper.getPropertyValue(name) == null),
                        Stream.of("id", "content", "class"))
                .distinct()
                .toArray(String[]::new);
    }
}
