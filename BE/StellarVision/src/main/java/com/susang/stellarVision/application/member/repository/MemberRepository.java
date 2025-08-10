package com.susang.stellarVision.application.member.repository;

import com.susang.stellarVision.application.member.dto.AuthProvider;
import com.susang.stellarVision.entity.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<Member> findByProviderAndProviderId(AuthProvider provider, String providerId);

    @Query("""
            SELECT m
              FROM Member m
              JOIN FETCH m.profile
             WHERE m.id = :memberId
              AND m.isDeleted = false
            """)
    Optional<Member> findByIdWithProfile(@Param("memberId") Long memberId);

    @Query("""
      SELECT m
      FROM Member m
      LEFT JOIN FETCH m.profile p
      WHERE (:name IS NULL OR m.name LIKE CONCAT(:name, '%'))
        AND (:cursor IS NULL OR m.id < :cursor)
      ORDER BY m.id DESC
    """)
    List<Member> findSliceByNameAndCursor(@Param("name") String name,
            @Param("cursor") Long cursor, Pageable pageable);
}
