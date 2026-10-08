package com.news.model.behavior.pojos;

import com.news.model.behavior.enums.ReactionType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
        name = "ap_article_reaction",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ap_article_reaction_user_article",
                columnNames = {"user_id", "article_id"}),
        indexes = {
            @Index(name = "idx_ap_article_reaction_article_type", columnList = "article_id,reaction_type")
        })
public class ApArticleReaction implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer userId;

    @Column(nullable = false)
    private Long articleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private ReactionType reactionType;

    @Column(nullable = false)
    private Date createdTime;

    @Column(nullable = false)
    private Date updatedTime;
}
