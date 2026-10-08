package com.news.model.user.vos;

import com.news.model.user.enums.CreatorApplicationStatus;
import lombok.Data;

import java.util.Date;

@Data
public class CreatorApplicationVo {

    private Integer id;
    private Integer userId;
    private String displayName;
    private String bio;
    private String category;
    private String portfolioUrl;
    private CreatorApplicationStatus status;
    private String reviewNote;
    private Date createdTime;
    private Date submittedTime;
    private Date reviewedTime;
    private Date updatedTime;
}
