package com.susang.stellarVision.application.member.repository;

import com.susang.stellarVision.entity.Member;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
    SELECT m
      FROM Member m
      JOIN FETCH m.profile
     WHERE m.id = :memberId
    """)
    Optional<Member> findByIdWithProfile(@Param("memberId") Long memberId);
}
