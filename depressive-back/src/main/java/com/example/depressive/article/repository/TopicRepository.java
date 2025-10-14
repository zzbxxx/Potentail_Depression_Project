package com.example.depressive.article.repository;

import com.example.depressive.article.entity.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TopicRepository extends JpaRepository<Topic, Long> {
    Optional<Topic> findByName(String name);

    Optional<Topic> findByNameAndCategoryIn(String topicName, List<Topic.Category> room);
}