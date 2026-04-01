package com.semotone.semotone.domain.user.repository;

import com.google.cloud.firestore.Firestore;
import com.semotone.semotone.domain.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    void save(String uid, UserEntity user);

    Optional<UserEntity> findById(String uid);

    boolean existsById(String uid);

    void addPointAndAcceptCount(String uid, int addPoint, int addAcceptCount);

    void usePoints(String uid, int amount);

    void addPostIdToUser(String uid, String postId);

    void removePostIdFromUser(String uid, String postId);

    void updateLocation(String uid, double latitude, double longitude);

    List<UserEntity> findAll();

    Firestore getDb();
}
