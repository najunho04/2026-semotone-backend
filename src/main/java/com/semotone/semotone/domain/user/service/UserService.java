package com.semotone.semotone.domain.user.service;

import com.semotone.semotone.domain.user.dto.UserCreateReqDto;
import com.semotone.semotone.domain.user.dto.UserResDto;

public interface UserService {
    /**
     * 1. 회원가입 처리
     * @param uid SecurityContext에서 꺼낸 검증된 유저 고유 ID
     * @param email SecurityContext에서 꺼낸 검증된 이메일
     * @param dto 클라이언트가 입력한 추가 정보 (닉네임, 위치 등)
     * @return 가입 완료된 유저의 정보 (바로 화면에 뿌려줄 수 있도록 반환)
     */
    UserResDto signUp(String uid, String email, UserCreateReqDto dto);

    /**
     * 2. 내 정보 조회
     * @param uid SecurityContext에서 꺼낸 내 고유 ID
     * @return 내 유저 정보
     */
    UserResDto getMyProfile(String uid);

    /**
     * 3. 다른 유저(남) 프로필 조회 (선택적)
     * 게시판에서 남의 글을 보고 그 사람의 신뢰도(매칭 횟수 등)를 확인할 때 사용합니다.
     * @param targetUid 조회하고 싶은 상대방의 고유 ID
     * @return 상대방 유저 정보
     */
    UserResDto getUserProfile(String targetUid);
}
