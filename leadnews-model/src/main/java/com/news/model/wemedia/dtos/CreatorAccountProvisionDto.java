package com.news.model.wemedia.dtos;

import lombok.Data;

@Data
public class CreatorAccountProvisionDto {

    private Integer apUserId;
    private String name;
    private String password;
    private String phone;
    private String image;
    private String nickname;
}
