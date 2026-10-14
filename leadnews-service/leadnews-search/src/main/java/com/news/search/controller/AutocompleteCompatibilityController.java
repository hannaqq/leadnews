package com.news.search.controller;

import com.news.model.common.dtos.ResponseResult;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

/**
 * Temporary compatibility endpoint for the existing compiled frontend.
 * Remove this controller after the frontend no longer requests autocomplete suggestions.
 */
@Deprecated(forRemoval = true)
@RestController
@RequestMapping("/api/v1/associate")
public class AutocompleteCompatibilityController {

    @PostMapping("/search")
    public ResponseResult<?> search() {
        return ResponseResult.okResult(Collections.emptyList());
    }
}
