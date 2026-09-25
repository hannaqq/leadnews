package com.news.model.wemedia.dtos;

import com.news.model.common.dtos.PageRequestDto;
import lombok.Data;

@Data
public class WmMaterialDto extends PageRequestDto {

    /**
     * 1 true
     * 0 false
     */
    private Short isCollection;
}
