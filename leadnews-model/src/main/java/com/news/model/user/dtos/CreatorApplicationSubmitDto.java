package com.news.model.user.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreatorApplicationSubmitDto {

    @NotBlank
    @Size(min = 2, max = 50)
    private String displayName;

    @NotBlank
    @Size(min = 20, max = 500)
    private String bio;

    @NotBlank
    @Size(min = 2, max = 50)
    private String category;

    @Size(max = 500)
    @Pattern(regexp = "^https?://.+$", message = "portfolioUrl must use HTTP or HTTPS")
    private String portfolioUrl;
}
