package com.susang.stellarVision.application.follow.repository;

import com.susang.stellarVision.entity.Follow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long>, FollowRepositoryCustom{
}
