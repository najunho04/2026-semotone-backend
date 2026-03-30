package com.semotone.semotone.domain.user.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import com.semotone.semotone.domain.user.entity.UserEntity;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
public class UserRepositoryImpl implements UserRepository {

    // Firestore의 컬렉션 이름을 상수로 빼두면 오타를 방지할 수 있습니다.
    private static final String COLLECTION_NAME = "users";

    /**
     * Firestore DB 인스턴스를 가져오는 헬퍼 메서드입니다.
     * FirebaseConfig에서 초기화가 정상적으로 끝났다면 여기서 바로 꺼내 쓸 수 있습니다.
     */
    @Override
    public Firestore getDb() {
        return FirestoreClient.getFirestore();
    }

    @Override
    public void save(String uid, UserEntity user) {
        try {
            // users 컬렉션에서 uid를 이름으로 하는 문서(Document)에 user 객체를 통째로 덮어씌웁니다.
            getDb().collection(COLLECTION_NAME).document(uid).set(user).get(); // .get()으로 비동기 작업이 끝날 때까지 기다립니다.
            log.info("Firestore: 유저 저장 완료 [UID: {}]", uid);
        } catch (Exception e) {
            log.error("Firestore 유저 저장 실패", e);
            throw new RuntimeException("DB 저장 중 오류가 발생했습니다.");
        }
    }

    @Override
    public Optional<UserEntity> findById(String uid) {
        try {
            DocumentReference docRef = getDb().collection(COLLECTION_NAME).document(uid);
            DocumentSnapshot document = docRef.get().get(); // DB에서 문서를 가져옵니다.

            if (document.exists()) {
                // 문서가 존재하면, 우리가 만든 UserEntity 클래스 모양으로 쏙 변환해서 반환합니다.
                return Optional.ofNullable(document.toObject(UserEntity.class));
            } else {
                // 문서가 없으면 빈 껍데기(Optional.empty)를 반환합니다.
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Firestore 유저 조회 실패", e);
            throw new RuntimeException("DB 조회 중 오류가 발생했습니다.");
        }
    }

    @Override
    public boolean existsById(String uid) {
        try {
            DocumentSnapshot document = getDb().collection(COLLECTION_NAME).document(uid).get().get();
            return document.exists();
        } catch (Exception e) {
            log.error("Firestore 유저 존재 여부 확인 실패", e);
            throw new RuntimeException("DB 조회 중 오류가 발생했습니다.");
        }
    }

    @Override
    public void addPointAndAcceptCount(String uid, int addPoint, int addAcceptCount) {
        try {
            Map<String, Object> updates = new HashMap<>();

            // ★ 핵심: 서버에서 계산하지 않고, DB 자체 명령어로 증감 처리
            updates.put("point", FieldValue.increment(addPoint));
            updates.put("acceptCount", FieldValue.increment(addAcceptCount));

            getDb().collection(COLLECTION_NAME).document(uid).update(updates).get();
            log.info("Firestore: 유저 포인트/수락횟수 증감 완료 [UID: {}, 변동: {}p]", uid, addPoint);
        } catch (Exception e) {
            log.error("Firestore 포인트 증감 실패", e);
            throw new RuntimeException("DB 업데이트 중 오류가 발생했습니다.");
        }
    }

    @Override
    public void addPostIdToUser(String uid, String postId) {
        try {
            // ★ NoSQL의 마법 (FieldValue.arrayUnion):
            // 기존 myPosts 배열을 자바로 다 가져와서 list.add()를 할 필요 없이,
            // DB 자체 명령어로 "이 배열에 이 값 하나만 추가해 줘!"라고 쏠 수 있습니다. (동시성 문제 완벽 방지)
            getDb().collection(COLLECTION_NAME).document(uid)
                    .update("myPosts", FieldValue.arrayUnion(postId)).get();
            log.info("Firestore: 유저 작성글 목록 추가 완료 [UID: {}, PostID: {}]", uid, postId);
        } catch (Exception e) {
            log.error("Firestore 유저 작성글 목록 업데이트 실패", e);
            throw new RuntimeException("DB 업데이트 중 오류가 발생했습니다.");
        }
    }
}