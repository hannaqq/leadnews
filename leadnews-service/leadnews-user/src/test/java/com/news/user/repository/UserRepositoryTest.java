package com.news.user.repository;

import com.news.model.user.pojos.ApUser;
import com.news.model.user.enums.CreatorApplicationStatus;
import com.news.model.user.pojos.CreatorApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class UserRepositoryTest {

    @Autowired private ApUserRepository userRepository;
    @Autowired private CreatorApplicationRepository applicationRepository;

    @Test
    void findsUserByPhone() {
        ApUser user = new ApUser();
        user.setPhone("5551000");
        userRepository.saveAndFlush(user);

        assertEquals(user.getId(), userRepository.findByPhone("5551000").orElseThrow().getId());
    }

    @Test
    void filtersAndPagesCreatorApplicationsUsingZeroBasedPageable() {
        saveApplication(10, "older", CreatorApplicationStatus.PENDING, new Date(1_000));
        saveApplication(11, "newer", CreatorApplicationStatus.PENDING, new Date(2_000));
        saveApplication(12, "other", CreatorApplicationStatus.REJECTED, new Date(3_000));

        var page = applicationRepository.findForReview(
                null, CreatorApplicationStatus.PENDING,
                PageRequest.of(0, 1, Sort.by(Sort.Direction.DESC, "submittedTime")));

        assertEquals(2, page.getTotalElements());
        assertEquals("newer", page.getContent().get(0).getDisplayName());
    }

    @Test
    void approvalRequiresPendingStatus() {
        CreatorApplication application = saveApplication(
                14, "creator", CreatorApplicationStatus.PENDING, new Date(4_000));

        assertEquals(1, applicationRepository.completeReview(
                application.getId(),
                CreatorApplicationStatus.PENDING,
                CreatorApplicationStatus.APPROVED,
                new Date(5_000)));
        assertEquals(0, applicationRepository.completeReview(
                application.getId(),
                CreatorApplicationStatus.PENDING,
                CreatorApplicationStatus.APPROVED,
                new Date(6_000)));
    }

    private CreatorApplication saveApplication(
            int userId,
            String displayName,
            CreatorApplicationStatus status,
            Date submittedTime) {
        CreatorApplication application = new CreatorApplication();
        application.setUserId(userId);
        application.setDisplayName(displayName);
        application.setBio("A sufficiently detailed creator biography");
        application.setCategory("News");
        application.setStatus(status);
        application.setCreatedTime(submittedTime);
        application.setSubmittedTime(submittedTime);
        application.setUpdatedTime(submittedTime);
        return applicationRepository.save(application);
    }

}
