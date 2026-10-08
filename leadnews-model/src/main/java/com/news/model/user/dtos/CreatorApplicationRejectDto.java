package com.news.model.user.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatorApplicationRejectDto {

    @NotBlank
    @Size(max = 500)
    private String reviewNote;
}
