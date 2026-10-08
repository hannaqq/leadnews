package com.news.model.user.pojos;

import com.news.model.user.enums.CreatorApplicationStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "ap_creator_application",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_creator_application_user_id",
                columnNames = "user_id")
)
public class CreatorApplication implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer userId;

    @Column(nullable = false, length = 50)
    private String displayName;

    @Column(nullable = false, length = 500)
    private String bio;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(length = 500)
    private String portfolioUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CreatorApplicationStatus status;

    @Column(length = 500)
    private String reviewNote;

    private Date createdTime;

    private Date submittedTime;

    private Date reviewedTime;

    private Date updatedTime;
}
