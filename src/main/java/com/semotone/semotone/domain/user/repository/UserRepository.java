package com.semotone.semotone.domain.user.repository;

import com.google.cloud.firestore.Firestore;
import com.semotone.semotone.domain.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    // 유저 DB 저장
    void save(String uid, UserEntity user);

    // userId로 User 객체 리턴
    Optional<UserEntity> findById(String uid);

    // userId로 User 객체 DB에 있는지 확인
    boolean existsById(String uid);

    // 포인트 지급 && acceptCount ++
    void addPointAndAcceptCount(String uid, int addPoint, int addAcceptCount);

    // (게시물 작성 시) 포인트 감소
    void usePoints(String uid, int amount);

    // (게시물 작성 시) myPosts에 postId 추가
    void addPostIdToUser(String uid, String postId);

    // (AI Res 에러 시 롤백 로직) 유저 DB에서 postId 삭제
    void removePostIdFromUser(String uid, String postId);

    // 유저 위치정보 동기화
    void updateLocation(String uid, double latitude, double longitude);

    // 유저 전체 조회
    List<UserEntity> findAll();

    Firestore getDb();
}
