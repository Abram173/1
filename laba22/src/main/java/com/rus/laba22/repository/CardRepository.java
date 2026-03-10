package com.rus.laba22.repository;

import com.rus.laba22.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CardRepository extends JpaRepository<Card, Long> {
}

