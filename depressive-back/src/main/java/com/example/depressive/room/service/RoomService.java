package com.example.depressive.room.service;

import com.example.depressive.login.entity.User;
import com.example.depressive.login.repository.UserRepository;
import com.example.depressive.room.dto.RoomReq;
import com.example.depressive.room.dto.RoomResp;
import com.example.depressive.room.entity.Room;
import com.example.depressive.room.entity.RoomTopic;
import com.example.depressive.room.repository.RoomRepository;
import com.example.depressive.room.repository.RoomTopicRepository;
import com.example.depressive.article.entity.Topic;
import com.example.depressive.article.repository.TopicRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private static final Logger logger = LoggerFactory.getLogger(RoomService.class);

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomTopicRepository roomTopicRepository;

    @Autowired
    private TopicRepository topicRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Transactional
    public RoomResp createRoom(RoomReq roomReq) {
        User user = userRepository.findById(roomReq.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("無效的用戶 ID: " + roomReq.getUserId()));
        if (roomReq.getName() == null || roomReq.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("房間名稱不能為空");
        }
        if (roomRepository.existsByNameAndStatus(roomReq.getName(), Room.RoomStatus.active)) {
            throw new IllegalArgumentException("房間名稱已存在");
        }
        if (roomReq.getType() == null || !isValidRoomType(roomReq.getType())) {
            throw new IllegalArgumentException("無效的房間類型: " + roomReq.getType());
        }
        if (roomReq.getTopics() != null && roomReq.getTopics().size() > 3) {
            throw new IllegalArgumentException("最多只能選擇 3 個標籤");
        }
        if (roomReq.getMaxUsers() == null || roomReq.getMaxUsers() < 1 || roomReq.getMaxUsers() > 6) {
            throw new IllegalArgumentException("最大人數必須在 1 到 6 之間");
        }
        if (roomReq.getAvatar() == null || roomReq.getAvatar().trim().isEmpty()) {
            throw new IllegalArgumentException("請選擇房間頭像");
        }

        Room room = new Room();
        room.setName(roomReq.getName());
        room.setType(Room.RoomType.valueOf(roomReq.getType().toLowerCase().replace(" ", "_").replace("-", "_")));
        room.setMaxUsers(roomReq.getMaxUsers());
        room.setCurrentUsers((byte) 1);
        room.setCreator(user);
        room.setStatus(Room.RoomStatus.active);
        room.setCreatedAt(LocalDateTime.now());
        room.setUpdatedAt(LocalDateTime.now());
        room.setEndTime(LocalDateTime.now().plusHours(1));
        room.setAvatar(roomReq.getAvatar());
        room = roomRepository.save(room);
        logger.info("房間創建成功: roomId={}, name={}", room.getId(), room.getName());

        // 處理標籤
        Set<Topic> topics = new HashSet<>();
        logger.info("接收到的標籤: {}", roomReq.getTopics());
        if (roomReq.getTopics() != null && !roomReq.getTopics().isEmpty()) {
            for (String topicName : roomReq.getTopics()) {
                logger.info("處理標籤: {}", topicName);
                try {
                    // 查找現有標籤，確保 category 匹配
                    Topic topic = topicRepository.findByNameAndCategoryIn(topicName, List.of(Topic.Category.room, Topic.Category.both))
                            .orElseGet(() -> {
                                Topic newTopic = new Topic();
                                newTopic.setName(topicName);
                                newTopic.setCreatedAt(LocalDateTime.now());
                                newTopic.setCategory(Topic.Category.room);
                                try {
                                    Topic savedTopic = topicRepository.save(newTopic);
                                    logger.info("創建新標籤: id={}, name={}", savedTopic.getId(), topicName);
                                    return savedTopic;
                                } catch (Exception e) {
                                    logger.error("創建標籤 {} 失敗: {}", topicName, e.getMessage());
                                    throw new RuntimeException("無法創建標籤: " + topicName, e);
                                }
                            });
                    topics.add(topic);
                    RoomTopic roomTopic = new RoomTopic();
                    roomTopic.setRoom(room);
                    roomTopic.setTopic(topic);
                    try {
                        roomTopicRepository.save(roomTopic);
                        logger.info("保存 RoomTopic 成功: roomId={}, topicId={}", room.getId(), topic.getId());
                    } catch (Exception e) {
                        logger.error("保存 RoomTopic 失敗: roomId={}, topicId={}", room.getId(), topic.getId(), e);
                        throw new RuntimeException("無法保存 RoomTopic: roomId=" + room.getId() + ", topicId=" + topic.getId(), e);
                    }
                } catch (Exception e) {
                    logger.error("處理標籤 {} 失敗: {}", topicName, e.getMessage());
                    throw new RuntimeException("處理標籤失敗: " + topicName, e);
                }
            }
        } else {
            logger.warn("未提供標籤");
        }

        String currentUsersKey = "room:" + room.getId() + ":current_users";
        String usersSetKey = "room:" + room.getId() + ":users";
        redisTemplate.opsForValue().set(currentUsersKey, "1");
        redisTemplate.opsForSet().add(usersSetKey, roomReq.getUserId().toString());
        redisTemplate.expire(currentUsersKey, 1, TimeUnit.HOURS);
        redisTemplate.expire(usersSetKey, 1, TimeUnit.HOURS);

        // 廣播人數更新
        messagingTemplate.convertAndSend("/topic/room/" + room.getId(), room.getCurrentUsers());

        RoomResp resp = new RoomResp();
        resp.setId(room.getId());
        resp.setName(room.getName());
        resp.setType(room.getType().name());
        resp.setTopics(topics.stream().map(Topic::getName).collect(Collectors.toSet()));
        resp.setMaxUsers(room.getMaxUsers());
        resp.setCurrentUsers(room.getCurrentUsers());
        resp.setCreatorId(user.getId());
        resp.setCreatorNickname(user.getNickname());
        resp.setCreatorAvatar(user.getAvatar());
        resp.setStatus(room.getStatus().name());
        resp.setCreatedAt(room.getCreatedAt());
        resp.setEndTime(room.getEndTime());
        resp.setAvatar(room.getAvatar());

        messagingTemplate.convertAndSend("/topic/rooms", resp);
        logger.info("房間創建響應: roomId={}, topics={}", room.getId(), resp.getTopics());
        return resp;
    }

    private boolean isValidRoomType(String type) {
        try {
            Room.RoomType.valueOf(type.toLowerCase().replace(" ", "_").replace("-", "_"));
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Transactional
    public List<RoomResp> getActiveRooms() {
        LocalDateTime now = LocalDateTime.now();
        List<Room> rooms = roomRepository.findByStatus(Room.RoomStatus.active);

        return rooms.stream()
                .filter(room -> room.getStatus() == Room.RoomStatus.active)
                .map(room -> {
                    RoomResp resp = new RoomResp();
                    resp.setId(room.getId());
                    resp.setName(room.getName());
                    resp.setType(room.getType().name());
                    resp.setMaxUsers(room.getMaxUsers());
                    resp.setCurrentUsers(room.getCurrentUsers());
                    resp.setCreatorId(room.getCreator().getId());
                    resp.setCreatorNickname(room.getCreator().getNickname());
                    resp.setCreatorAvatar(room.getCreator().getAvatar());
                    resp.setStatus(room.getStatus().name());
                    resp.setCreatedAt(room.getCreatedAt());
                    resp.setEndTime(room.getEndTime());
                    resp.setAvatar(room.getAvatar());

                    String currentUsersKey = "room:" + room.getId() + ":current_users";
                    try {
                        String currentUsers = redisTemplate.opsForValue().get(currentUsersKey);
                        if (currentUsers != null) {
                            resp.setCurrentUsers(Byte.parseByte(currentUsers));
                        }
                    } catch (Exception e) {
                        logger.error("Failed to fetch current users from Redis for room {}: {}", room.getId(), e.getMessage());
                    }

                    List<RoomTopic> roomTopics = roomTopicRepository.findByRoomId(room.getId());
                    Set<String> topics = roomTopics.stream()
                            .map(roomTopic -> roomTopic.getTopic().getName())
                            .collect(Collectors.toSet());
                    resp.setTopics(topics);

                    return resp;
                }).collect(Collectors.toList());
    }

    @Transactional
    public RoomResp getRoomById(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("房間 ID 不存在: " + roomId));

        RoomResp resp = new RoomResp();
        resp.setId(room.getId());
        resp.setName(room.getName());
        resp.setType(room.getType().name());
        resp.setMaxUsers(room.getMaxUsers());
        resp.setCurrentUsers(room.getCurrentUsers());
        resp.setCreatorId(room.getCreator().getId());
        resp.setCreatorNickname(room.getCreator().getNickname());
        resp.setCreatorAvatar(room.getCreator().getAvatar());
        resp.setStatus(room.getStatus().name());
        resp.setCreatedAt(room.getCreatedAt());
        resp.setEndTime(room.getEndTime());
        resp.setAvatar(room.getAvatar());

        String currentUsersKey = "room:" + room.getId() + ":current_users";
        try {
            String currentUsers = redisTemplate.opsForValue().get(currentUsersKey);
            if (currentUsers != null) {
                resp.setCurrentUsers(Byte.parseByte(currentUsers));
            }
        } catch (Exception e) {
            logger.error("Failed to fetch current users from Redis for room {}: {}", roomId, e.getMessage());
        }

        List<RoomTopic> roomTopics = roomTopicRepository.findByRoomId(room.getId());
        Set<String> topics = roomTopics.stream()
                .map(roomTopic -> roomTopic.getTopic().getName())
                .collect(Collectors.toSet());
        resp.setTopics(topics);

        return resp;
    }

    @Transactional
    public RoomResp joinRoom(Long roomId, Long userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("房間 ID 不存在: " + roomId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用戶 ID 不存在: " + userId));

        if (room.getStatus() != Room.RoomStatus.active) {
            throw new IllegalArgumentException("房間已關閉或無效: " + roomId);
        }

        String currentUsersKey = "room:" + roomId + ":current_users";
        String usersSetKey = "room:" + roomId + ":users";
        try {
            // 檢查用戶是否已在房間中
            Boolean isMember = redisTemplate.opsForSet().isMember(usersSetKey, userId.toString());
            if (Boolean.TRUE.equals(isMember)) {
                // 用戶已在房間中，直接返回房間數據
                RoomResp resp = new RoomResp();
                resp.setId(room.getId());
                resp.setName(room.getName());
                resp.setType(room.getType().name());
                resp.setMaxUsers(room.getMaxUsers());
                resp.setCurrentUsers(room.getCurrentUsers());
                resp.setCreatorId(room.getCreator().getId());
                resp.setCreatorNickname(room.getCreator().getNickname());
                resp.setCreatorAvatar(room.getCreator().getAvatar());
                resp.setStatus(room.getStatus().name());
                resp.setCreatedAt(room.getCreatedAt());
                resp.setEndTime(room.getEndTime());
                resp.setAvatar(room.getAvatar());

                List<RoomTopic> roomTopics = roomTopicRepository.findByRoomId(room.getId());
                Set<String> topics = roomTopics.stream()
                        .map(roomTopic -> roomTopic.getTopic().getName())
                        .collect(Collectors.toSet());
                resp.setTopics(topics);

                // 更新 Redis 中的人數（以防數據不同步）
                String currentUsersStr = redisTemplate.opsForValue().get(currentUsersKey);
                if (currentUsersStr != null) {
                    resp.setCurrentUsers(Byte.parseByte(currentUsersStr));
                }
                return resp;
            }

            // 檢查房間是否已滿
            String currentUsersStr = redisTemplate.opsForValue().get(currentUsersKey);
            int currentUsers = currentUsersStr != null ? Integer.parseInt(currentUsersStr) : room.getCurrentUsers();
            if (currentUsers >= room.getMaxUsers()) {
                throw new IllegalArgumentException("房間已滿: " + roomId);
            }

            // 增加人數並記錄用戶
            redisTemplate.opsForValue().increment(currentUsersKey, 1);
            redisTemplate.opsForSet().add(usersSetKey, userId.toString());
            redisTemplate.expire(usersSetKey, 1, TimeUnit.HOURS);

            // 更新房間數據庫
            room.setCurrentUsers((byte) (currentUsers + 1));
            room.setUpdatedAt(LocalDateTime.now());
            roomRepository.save(room);

            // 廣播人數更新
            messagingTemplate.convertAndSend("/topic/room/" + roomId, room.getCurrentUsers());

            // 構建響應
            RoomResp resp = new RoomResp();
            resp.setId(room.getId());
            resp.setName(room.getName());
            resp.setType(room.getType().name());
            resp.setMaxUsers(room.getMaxUsers());
            resp.setCurrentUsers((byte) (currentUsers + 1));
            resp.setCreatorId(room.getCreator().getId());
            resp.setCreatorNickname(room.getCreator().getNickname());
            resp.setCreatorAvatar(room.getCreator().getAvatar());
            resp.setStatus(room.getStatus().name());
            resp.setCreatedAt(room.getCreatedAt());
            resp.setEndTime(room.getEndTime());
            resp.setAvatar(room.getAvatar());

            List<RoomTopic> roomTopics = roomTopicRepository.findByRoomId(room.getId());
            Set<String> topics = roomTopics.stream()
                    .map(roomTopic -> roomTopic.getTopic().getName())
                    .collect(Collectors.toSet());
            resp.setTopics(topics);

            return resp;
        } catch (Exception e) {
            logger.error("Failed to join room {} for user {}: {}", roomId, userId, e.getMessage());
            throw e;
        }
    }

    @Transactional
    public RoomResp leaveRoom(Long roomId, Long userId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("房間 ID 不存在: " + roomId));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用戶 ID 不存在: " + userId));

        String currentUsersKey = "room:" + roomId + ":current_users";
        String usersSetKey = "room:" + roomId + ":users";
        try {
            // 檢查用戶是否在房間中
            Boolean isMember = redisTemplate.opsForSet().isMember(usersSetKey, userId.toString());
            if (Boolean.FALSE.equals(isMember)) {
                throw new IllegalArgumentException("用戶不在房間中: " + userId);
            }

            // 減少人數並移除用戶
            String currentUsersStr = redisTemplate.opsForValue().get(currentUsersKey);
            int currentUsers = currentUsersStr != null ? Integer.parseInt(currentUsersStr) : room.getCurrentUsers();
            if (currentUsers > 0) {
                redisTemplate.opsForValue().decrement(currentUsersKey, 1);
                redisTemplate.opsForSet().remove(usersSetKey, userId.toString());

                // 更新房間數據庫
                room.setCurrentUsers((byte) (currentUsers - 1));
                room.setUpdatedAt(LocalDateTime.now());
                roomRepository.save(room);

                // 檢查是否所有人都離開了房間
                if (currentUsers - 1 == 0) {
                    // 所有人都離開，關閉房間
                    return closeRoom(roomId);
                }

                // 廣播人數更新
                messagingTemplate.convertAndSend("/topic/room/" + roomId, room.getCurrentUsers());
            }

            // 構建響應
            RoomResp resp = new RoomResp();
            resp.setId(room.getId());
            resp.setName(room.getName());
            resp.setType(room.getType().name());
            resp.setMaxUsers(room.getMaxUsers());
            resp.setCurrentUsers((byte) (currentUsers - 1));
            resp.setCreatorId(room.getCreator().getId());
            resp.setCreatorNickname(room.getCreator().getNickname());
            resp.setCreatorAvatar(room.getCreator().getAvatar());
            resp.setStatus(room.getStatus().name());
            resp.setCreatedAt(room.getCreatedAt());
            resp.setEndTime(room.getEndTime());
            resp.setAvatar(room.getAvatar());

            List<RoomTopic> roomTopics = roomTopicRepository.findByRoomId(room.getId());
            Set<String> topics = roomTopics.stream()
                    .map(roomTopic -> roomTopic.getTopic().getName())
                    .collect(Collectors.toSet());
            resp.setTopics(topics);

            return resp;
        } catch (Exception e) {
            logger.error("Failed to leave room {} for user {}: {}", roomId, userId, e.getMessage());
            throw e;
        }
    }

    @Transactional
    public RoomResp closeRoom(Long roomId) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("房間 ID 不存在: " + roomId));

        if (room.getStatus() == Room.RoomStatus.closed) {
            throw new IllegalArgumentException("房間已關閉: " + roomId);
        }

        room.setStatus(Room.RoomStatus.closed);
        room.setUpdatedAt(LocalDateTime.now());
        room.setAvatar("");
        roomRepository.save(room);
        logger.info("Room ID {} status updated to closed", roomId);

        String currentUsersKey = "room:" + roomId + ":current_users";
        String usersSetKey = "room:" + roomId + ":users";
        try {
            redisTemplate.delete(currentUsersKey);
            redisTemplate.delete(usersSetKey);
            logger.info("Redis data cleared for room ID {}", roomId);

            // 廣播房間關閉
            messagingTemplate.convertAndSend("/topic/room/" + roomId, "closed");
        } catch (Exception e) {
            logger.error("Failed to clear Redis data for room {}: {}", roomId, e.getMessage());
        }

        RoomResp resp = new RoomResp();
        resp.setId(room.getId());
        resp.setName(room.getName());
        resp.setType(room.getType().name());
        resp.setMaxUsers(room.getMaxUsers());
        resp.setCurrentUsers(room.getCurrentUsers());
        resp.setCreatorId(room.getCreator().getId());
        resp.setCreatorNickname(room.getCreator().getNickname());
        resp.setCreatorAvatar(room.getCreator().getAvatar());
        resp.setStatus(room.getStatus().name());
        resp.setCreatedAt(room.getCreatedAt());
        resp.setEndTime(room.getEndTime());
        resp.setAvatar(room.getAvatar());

        List<RoomTopic> roomTopics = roomTopicRepository.findByRoomId(room.getId());
        Set<String> topics = roomTopics.stream()
                .map(roomTopic -> roomTopic.getTopic().getName())
                .collect(Collectors.toSet());
        resp.setTopics(topics);

        return resp;
    }

    @Transactional
    public RoomResp getCurrentRoom(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用戶 ID 不存在: " + userId));

        List<Room> activeRooms = roomRepository.findByStatus(Room.RoomStatus.active);
        for (Room room : activeRooms) {
            String usersSetKey = "room:" + room.getId() + ":users";
            Boolean isMember = redisTemplate.opsForSet().isMember(usersSetKey, userId.toString());
            if (Boolean.TRUE.equals(isMember)) {
                RoomResp resp = new RoomResp();
                resp.setId(room.getId());
                resp.setName(room.getName());
                resp.setType(room.getType().name());
                resp.setMaxUsers(room.getMaxUsers());
                resp.setCurrentUsers(room.getCurrentUsers());
                resp.setCreatorId(room.getCreator().getId());
                resp.setCreatorNickname(room.getCreator().getNickname());
                resp.setCreatorAvatar(room.getCreator().getAvatar());
                resp.setStatus(room.getStatus().name());
                resp.setCreatedAt(room.getCreatedAt());
                resp.setEndTime(room.getEndTime());
                resp.setAvatar(room.getAvatar());

                String currentUsersKey = "room:" + room.getId() + ":current_users";
                try {
                    String currentUsers = redisTemplate.opsForValue().get(currentUsersKey);
                    if (currentUsers != null) {
                        resp.setCurrentUsers(Byte.parseByte(currentUsers));
                    }
                } catch (Exception e) {
                    logger.error("Failed to fetch current users from Redis for room {}: {}", room.getId(), e.getMessage());
                }

                List<RoomTopic> roomTopics = roomTopicRepository.findByRoomId(room.getId());
                Set<String> topics = roomTopics.stream()
                        .map(roomTopic -> roomTopic.getTopic().getName())
                        .collect(Collectors.toSet());
                resp.setTopics(topics);

                return resp;
            }
        }
        return null; // 用戶不在任何活躍房間
    }
}