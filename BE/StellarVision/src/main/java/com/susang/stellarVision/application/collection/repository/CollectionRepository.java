package com.susang.stellarVision.application.collection.repository;

import com.susang.stellarVision.entity.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CollectionRepository extends JpaRepository<Collection, Long> {

    List<Collection> findByAbbreviationIn(java.util.Collection<String> abbreviations);
}