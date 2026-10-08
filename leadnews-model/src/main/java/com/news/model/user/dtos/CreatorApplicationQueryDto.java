package com.news.model.user.dtos;

import com.news.model.user.enums.CreatorApplicationStatus;
import lombok.Data;

@Data
public class CreatorApplicationQueryDto {

    private Integer id;
    private CreatorApplicationStatus status;
    private Integer page;
    private Integer size;

    public void checkParam() {
        if (page == null || page < 1) {
            page = 1;
        }
        if (size == null || size < 1 || size > 100) {
            size = 10;
        }
    }
}
