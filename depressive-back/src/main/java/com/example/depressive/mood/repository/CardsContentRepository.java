package com.example.depressive.mood.repository;

import com.example.depressive.mood.entity.CardsContent;
import com.example.depressive.mood.entity.UserCardsLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CardsContentRepository extends JpaRepository<CardsContent, Short> {
    List<CardsContent> findByIsActiveTrue();

    boolean existsById(long id);
}