package com.news.behavior.service.impl;

import com.news.behavior.repository.ApFollowBehaviorRepository;
import com.news.behavior.service.ApBehaviorEntryService;
import com.news.common.constants.SystemConstants;
import com.news.model.behavior.dtos.FollowBehaviorDto;
import com.news.model.behavior.pojos.ApBehaviorEntry;
import com.news.model.behavior.pojos.ApFollowBehavior;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ApFollowBehaviorServiceImplTest {

    private final ApBehaviorEntryService entryService = mock(ApBehaviorEntryService.class);
    private final ApFollowBehaviorRepository repository = mock(ApFollowBehaviorRepository.class);
    private final ApFollowBehaviorServiceImpl service =
            new ApFollowBehaviorServiceImpl(entryService, repository);

    @Test
    void mapsAndSavesFollowEventWhenEntryExists() {
        FollowBehaviorDto dto = dto();
        ApBehaviorEntry entry = new ApBehaviorEntry();
        entry.setEntryId(12);
        when(entryService.findByUserIdOrEquipmentId(12, SystemConstants.TYPE_USER))
                .thenReturn(entry);

        service.saveFollowBehavior(dto);

        ArgumentCaptor<ApFollowBehavior> captor = ArgumentCaptor.forClass(ApFollowBehavior.class);
        verify(repository).save(captor.capture());
        ApFollowBehavior saved = captor.getValue();
        assertEquals(12, saved.getEntryId());
        assertEquals(33, saved.getFollowId());
        assertEquals(44L, saved.getArticleId());
        assertNotNull(saved.getCreatedTime());
    }

    @Test
    void doesNotSaveFollowEventWhenEntryIsMissing() {
        FollowBehaviorDto dto = dto();
        when(entryService.findByUserIdOrEquipmentId(12, SystemConstants.TYPE_USER))
                .thenReturn(null);

        service.saveFollowBehavior(dto);

        verify(repository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private static FollowBehaviorDto dto() {
        FollowBehaviorDto dto = new FollowBehaviorDto();
        dto.setUserId(12);
        dto.setFollowId(33);
        dto.setArticleId(44L);
        return dto;
    }
}
