package com.semotone.semotone.domain.user.repository;

import com.google.cloud.firestore.Firestore;
import com.semotone.semotone.domain.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    /**
     * 1. 신규 유저 저장 및 기존 유저 덮어쓰기
     * - 회원가입 완료 후 초기 데이터를 Firestore에 Document로 생성할 때 사용합니다.
     */
    void save(String uid, UserEntity user);

    /**
     * 2. UID로 유저 정보 조회
     * - 내 정보 보기, 남의 프로필 보기, 혹은 게시글 수락 전 포인트가 충분한지 검증할 때 사용합니다.
     * - DB에 유저가 없을 수도 있으므로 NullPointerException 방지를 위해 Optional로 감싸는 것이 정석입니다.
     */
    Optional<UserEntity> findById(String uid);

    /**
     * 3. 유저 가입 여부 확인
     * - 클라이언트가 로그인 토큰을 보냈을 때, 이 유저가 DB에 등록된 유저인지(회원가입을 마쳤는지)
     * 빠르게 체크하기 위해 사용합니다.
     */
    boolean existsById(String uid);

    /**
     * 4. 유저 포인트 및 수락 횟수 증가 (동시성 완벽 방어)
     * - 기존 값을 읽지 않고, Firestore의 increment를 사용해 증감분만 전달합니다.
     * - 포인트를 차감할 때는 addPoint에 음수(-50)를 넣으면 됩니다.
     */
    void addPointAndAcceptCount(String uid, int addPoint, int addAcceptCount);

    /**
     * 5. 작성한 게시글 ID 추가 (배열 업데이트)
     * - 유저가 새로운 게시글을 작성했을 때, User 문서 안의 myPosts 배열에 새로운 postId를 밀어 넣습니다.
     * -> ** post 로직과 엮여야 해서 아직 미개발
     */
    void addPostIdToUser(String uid, String postId);

    /**
     * 6. 클라이언트 위치 정보 최신화
     * - n초에 한번씩 오는 위치 동기화 로직에 대한 DB (Location) 최신화
     */
    void updateLocation(String uid, double latitude, double longitude);


    /**
     * 7. 전체 유저 목록 조회
     */
    List<UserEntity> findAll();

    Firestore getDb();
}
