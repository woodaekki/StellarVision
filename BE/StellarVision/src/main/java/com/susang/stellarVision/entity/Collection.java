package com.susang.stellarVision.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "collections",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "UK_COLLECTION_ABBR",
                        columnNames = {"abbreviation"}
                ),
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Collection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", nullable = false, length = 100)
    private String name; // 영문 이름 (예: Aries)

    @Column(name = "korean_name", nullable = false, length = 100)
    private String koreanName; // 한국어 이름 (예: 양자리)

    @Column(name = "abbreviation", nullable = false, length = 10)
    private String abbreviation; // 약어 (예: Ari)

    @Column(name = "collection_count", nullable = false)
    private Long collectionCount;

    @Builder
    public Collection(String name, String koreanName, String abbreviation, Long collectionCount) {
        this.name = name;
        this.koreanName = koreanName;
        this.abbreviation = abbreviation;
        this.collectionCount = collectionCount;
    }
}