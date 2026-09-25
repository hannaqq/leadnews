package com.news.wemedia.service;

import com.news.wemedia.WemediaApplication;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@SpringBootTest(classes = WemediaApplication.class)
@ExtendWith(SpringExtension.class)
public class WmNewsAutoScanServiceTest {
    @Autowired
    private WmNewsAutoScanService wmNewsAutoScanService;
    @Test
    void autoScanWmNews() {
        wmNewsAutoScanService.autoScanWmNews(6238);
    }
}