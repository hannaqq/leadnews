package com.news.model.behavior.pojos;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
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
        name = "ap_user_follow",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ap_user_follow_user_creator",
                columnNames = {"user_id", "creator_id"}),
        indexes = @Index(
                name = "idx_ap_user_follow_creator_id",
                columnList = "creator_id"))
public class ApUserFollow implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer userId;
    private Integer creatorId;
    private Date createdTime;
}
