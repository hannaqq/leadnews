package com.news.wemedia.repository;

import com.news.common.exception.CustomException;
import com.news.model.wemedia.pojos.WmMaterial;
import com.news.model.wemedia.pojos.WmNews;
import com.news.model.wemedia.pojos.WmNewsMaterial;
import com.news.model.wemedia.pojos.WmUser;
import com.news.wemedia.service.impl.WemediaPersistenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import(WemediaPersistenceService.class)
class WemediaRepositoryTest {
    @Autowired private WmMaterialRepository materialRepository;
    @Autowired private WmNewsRepository newsRepository;
    @Autowired private WmNewsMaterialRepository relationRepository;
    @Autowired private WmUserRepository userRepository;
    @Autowired private WemediaPersistenceService persistenceService;

    @Test
    void savesMaterialRelationsInRequestOrder() {
        WmMaterial first = material("first");
        WmMaterial second = material("second");
        materialRepository.saveAll(List.of(first, second));

        WmNews saved = persistenceService.saveNewsAndRelations(
                news(), 1, List.of("second", "first"), List.of(), (short) 0, (short) 1);

        List<WmNewsMaterial> relations =
                relationRepository.findByNewsIdOrderByTypeAscOrdAsc(saved.getId());
        assertEquals(2, relations.size());
        assertEquals(second.getId(), relations.get(0).getMaterialId());
        assertEquals((short) 0, relations.get(0).getOrd());
        assertEquals(first.getId(), relations.get(1).getMaterialId());
        assertEquals((short) 1, relations.get(1).getOrd());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void rollsBackNewsWhenAnyMaterialIsMissing() {
        materialRepository.save(material("first"));

        assertThrows(CustomException.class, () -> persistenceService.saveNewsAndRelations(
                news(), 1, List.of("first", "missing"), List.of(), (short) 0, (short) 1));

        assertTrue(newsRepository.findAll().isEmpty());
        assertTrue(relationRepository.findAll().isEmpty());
    }

    @Test
    void filtersAndPagesNewsUsingOneBasedApiConversion() {
        WmNews first = news();
        first.setTitle("spring first");
        first.setPublishTime(new Date(1_000));
        first.setChannelId(7);
        WmNews second = news();
        second.setTitle("spring second");
        second.setPublishTime(new Date(2_000));
        second.setChannelId(7);
        WmNews other = news();
        other.setTitle("other");
        other.setPublishTime(new Date(3_000));
        other.setChannelId(8);
        newsRepository.saveAll(List.of(first, second, other));

        var page = newsRepository.findForUser(
                1, WmNews.Status.SUBMIT.getCode(), 7, "spring", null, null,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "publishTime")));

        assertEquals(2, page.getTotalElements());
        assertEquals("spring second", page.getContent().get(0).getTitle());
    }

    @Test
    void enforcesOneWemediaAccountPerApplicationUser() {
        userRepository.saveAndFlush(user(10));

        assertThrows(RuntimeException.class, () -> userRepository.saveAndFlush(user(10)));
    }

    @Test
    void rejectsMaterialOwnedByAnotherUser() {
        WmMaterial material = material("private");
        material.setUserId(2);
        materialRepository.save(material);

        assertThrows(CustomException.class, () -> persistenceService.saveNewsAndRelations(
                news(), 1, List.of("private"), List.of(), (short) 0, (short) 1));
    }

    @Test
    void rejectsUpdatingAnotherUsersNews() {
        WmNews existing = news();
        existing.setUserId(2);
        existing = newsRepository.save(existing);
        WmNews update = new WmNews();
        update.setId(existing.getId());
        update.setUserId(1);
        update.setTitle("hijacked");

        assertThrows(RuntimeException.class, () -> persistenceService.saveNewsAndRelations(
                update, 1, List.of(), List.of(), (short) 0, (short) 1));

        assertEquals("title", newsRepository.findById(existing.getId()).orElseThrow().getTitle());
    }

    @Test
    void allowsOnlyOneWorkerToClaimSubmittedNews() {
        WmNews news = newsRepository.saveAndFlush(news());

        assertTrue(persistenceService.claimNewsForProcessing(news.getId()));
        assertTrue(!persistenceService.claimNewsForProcessing(news.getId()));
        assertEquals(WmNews.Status.PROCESSING.getCode(),
                newsRepository.findById(news.getId()).orElseThrow().getStatus());
    }

    @Test
    void doesNotOverwriteStatusChangedAfterProcessingStarted() {
        WmNews news = newsRepository.saveAndFlush(news());
        assertTrue(persistenceService.claimNewsForProcessing(news.getId()));
        assertTrue(persistenceService.transitionProcessingStatus(
                news.getId(), WmNews.Status.ADMIN_AUTH.getCode(), "manual review"));

        assertTrue(!persistenceService.completePublishing(news.getId(), 99L));
        WmNews unchanged = newsRepository.findById(news.getId()).orElseThrow();
        assertEquals(WmNews.Status.ADMIN_AUTH.getCode(), unchanged.getStatus());
        assertEquals(null, unchanged.getArticleId());
    }

    private static WmMaterial material(String url) {
        WmMaterial material = new WmMaterial();
        material.setUserId(1);
        material.setUrl(url);
        material.setType((short) 0);
        material.setIsCollection((short) 0);
        material.setCreatedTime(new Date());
        return material;
    }

    private static WmNews news() {
        WmNews news = new WmNews();
        news.setUserId(1);
        news.setTitle("title");
        news.setContent("[]");
        news.setStatus(WmNews.Status.SUBMIT.getCode());
        news.setCreatedTime(new Date());
        return news;
    }

    private static WmUser user(int apUserId) {
        WmUser user = new WmUser();
        user.setApUserId(apUserId);
        user.setName("user");
        user.setCreatedTime(new Date());
        return user;
    }
}
