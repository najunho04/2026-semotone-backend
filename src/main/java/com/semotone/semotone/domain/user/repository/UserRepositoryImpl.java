package com.semotone.semotone.domain.user.repository;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.semotone.semotone.domain.user.entity.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private static final String COLLECTION_NAME = "users";
    private final Firestore firestore;

    @Override
    public List<UserEntity> findAll() {
        try {
            QuerySnapshot querySnapshot = getDb().collection(COLLECTION_NAME).get().get();
            List<UserEntity> users = new ArrayList<>();
            for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
                users.add(doc.toObject(UserEntity.class));
            }
            log.info("Firestore: all users fetched [count: {}]", users.size());
            return users;
        } catch (Exception e) {
            log.error("Firestore all users fetch failed", e);
            throw new RuntimeException("DB 조회 중 오류가 발생했습니다.");
        }
    }

    @Override
    public Firestore getDb() {
        return firestore;
    }

    @Override
    public void save(String uid, UserEntity user) {
        try {
            getDb().collection(COLLECTION_NAME).document(uid).set(user).get();
            log.info("Firestore: user saved [uid: {}]", uid);
        } catch (Exception e) {
            log.error("Firestore user save failed", e);
            throw new RuntimeException("DB 저장 중 오류가 발생했습니다.");
        }
    }

    @Override
    public Optional<UserEntity> findById(String uid) {
        try {
            DocumentReference docRef = getDb().collection(COLLECTION_NAME).document(uid);
            DocumentSnapshot document = docRef.get().get();
            if (document.exists()) {
                return Optional.ofNullable(document.toObject(UserEntity.class));
            }
            return Optional.empty();
        } catch (Exception e) {
            log.error("Firestore user lookup failed", e);
            throw new RuntimeException("DB 조회 중 오류가 발생했습니다.");
        }
    }

    @Override
    public boolean existsById(String uid) {
        try {
            DocumentSnapshot document = getDb().collection(COLLECTION_NAME).document(uid).get().get();
            return document.exists();
        } catch (Exception e) {
            log.error("Firestore user exists check failed", e);
            throw new RuntimeException("DB 조회 중 오류가 발생했습니다.");
        }
    }

    @Override
    public void addPointAndAcceptCount(String uid, int addPoint, int addAcceptCount) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("point", FieldValue.increment(addPoint));
            updates.put("acceptCount", FieldValue.increment(addAcceptCount));
            getDb().collection(COLLECTION_NAME).document(uid).update(updates).get();
            log.info("Firestore: user point/acceptCount updated [uid: {}, pointDelta: {}, acceptDelta: {}]",
                    uid, addPoint, addAcceptCount);
        } catch (Exception e) {
            log.error("Firestore point update failed", e);
            throw new RuntimeException("DB 업데이트 중 오류가 발생했습니다.");
        }
    }

    @Override
    public void usePoints(String uid, int amount) {
        if (amount <= 0) {
            throw new RuntimeException("차감할 포인트는 1 이상이어야 합니다.");
        }

        try {
            DocumentReference docRef = getDb().collection(COLLECTION_NAME).document(uid);
            getDb().runTransaction(transaction -> {
                DocumentSnapshot snapshot = transaction.get(docRef).get();
                Long currentPoint = snapshot.getLong("point");

                if (currentPoint == null) {
                    throw new RuntimeException("사용자 포인트 정보를 찾을 수 없습니다.");
                }

                if (currentPoint < amount) {
                    throw new RuntimeException("포인트가 부족합니다.");
                }

                transaction.update(docRef, "point", currentPoint - amount);
                return null;
            }).get();
            log.info("Firestore: user points deducted [uid: {}, amount: {}]", uid, amount);
        } catch (Exception e) {
            log.error("Firestore point deduction failed", e);
            if (e.getCause() instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            throw new RuntimeException("DB 업데이트 중 오류가 발생했습니다.");
        }
    }

    @Override
    public void addPostIdToUser(String uid, String postId) {
        try {
            getDb().collection(COLLECTION_NAME).document(uid)
                    .update("myPosts", FieldValue.arrayUnion(postId)).get();
            log.info("Firestore: user post added [uid: {}, postId: {}]", uid, postId);
        } catch (Exception e) {
            log.error("Firestore user post add failed", e);
            throw new RuntimeException("DB 업데이트 중 오류가 발생했습니다.");
        }
    }

    @Override
    public void removePostIdFromUser(String uid, String postId) {
        try {
            getDb().collection(COLLECTION_NAME).document(uid)
                    .update("myPosts", FieldValue.arrayRemove(postId)).get();
            log.info("Firestore: user post removed [uid: {}, postId: {}]", uid, postId);
        } catch (Exception e) {
            log.error("Firestore user post remove failed", e);
            throw new RuntimeException("DB 업데이트 중 오류가 발생했습니다.");
        }
    }

    @Override
    public void updateLocation(String uid, double latitude, double longitude) {
        try {
            Map<String, Object> updates = new HashMap<>();
            updates.put("latitude", latitude);
            updates.put("longitude", longitude);
            getDb().collection(COLLECTION_NAME).document(uid).update(updates).get();
            log.info("Firestore: user location updated [uid: {}]", uid);
        } catch (Exception e) {
            log.error("Firestore location update failed", e);
            throw new RuntimeException("위치 정보 업데이트 중 오류가 발생했습니다.");
        }
    }
}
