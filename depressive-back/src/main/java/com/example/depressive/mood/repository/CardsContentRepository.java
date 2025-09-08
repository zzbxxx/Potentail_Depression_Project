package com.example.depressive.mood.repository;

import com.example.depressive.mood.entity.CardsContent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CardsContentRepository extends JpaRepository<CardsContent, Short> {
    List<CardsContent> findByIsActiveTrue();
}