package com.example.depressive.room.entity;

import com.example.depressive.article.entity.Topic;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "room_topics")
@IdClass(RoomTopicId.class)
@Getter
@Setter
@NoArgsConstructor
public class RoomTopic {

    @Id
    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Id
    @ManyToOne
    @JoinColumn(name = "topic_id", nullable = false)
    private Topic topic;
}