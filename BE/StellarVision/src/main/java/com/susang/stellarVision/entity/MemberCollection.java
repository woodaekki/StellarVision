package com.susang.stellarVision.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "members_collections")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberCollection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Column(name = "is_select", nullable = false)
    private boolean isSelect;

    @ManyToOne
    @JoinColumn(
            name = "collection_id",
            nullable = false
    )
    private Collection collection;

    @ManyToOne
    @JoinColumn(
            name = "member_id",
            nullable = false
    )
    private Member member;

    @Builder
    public MemberCollection(Collection collection, Member member) {
        this.isSelect = false;
        this.collection = collection;
        this.member = member;
    }
}
