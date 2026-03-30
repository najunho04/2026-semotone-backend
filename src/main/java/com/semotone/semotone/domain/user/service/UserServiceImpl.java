package com.semotone.semotone.domain.user.service;


import com.semotone.semotone.domain.user.dto.UserCreateReqDto;
import com.semotone.semotone.domain.user.dto.UserResDto;
import com.semotone.semotone.domain.user.entity.UserEntity;
import com.semotone.semotone.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResDto signUp(String uid, String email, UserCreateReqDto dto) {

        // 1. 이미 가입된 유저인지 검증 (선택적이지만 안전을 위해!)
        if (userRepository.existsById(uid)) {
            throw new RuntimeException("이미 가입된 유저입니다.");
        }

        // 2. DTO + Token 정보를 합쳐서 DB에 넣을 Entity로 조립
        UserEntity userEntity = UserEntity.builder()
                .nickName(dto.getNickName())
                .gmail(email)          // 토큰에서 뽑은 진짜 이메일
                .point(1000)           // ★ 신규 가입 보너스 1000 포인트!
                .acceptCount(0)        // 초기 수락 횟수는 0
                // .latitude(dto.getLatitude()) // 나중에 엔티티에 위치 필드 추가하셨다면 주석 해제!
                // .longitude(dto.getLongitude())
                .build();

        // 3. 조립된 Entity를 DB에 저장
        userRepository.save(uid, userEntity);

        // 4. 클라이언트(Flutter)에게 응답해줄 DTO로 다시 예쁘게 포장해서 리턴
        return UserResDto.builder()
                .uid(uid)
                .nickName(userEntity.getNickName())
                .gmail(userEntity.getGmail())
                .point(userEntity.getPoint())
                .acceptCount(userEntity.getAcceptCount())
                .build();
    }

    // 아래 두 메서드는 일단 빈 껍데기만 만들어둡니다. (인터페이스 구현 규칙)
    @Override
    public UserResDto getMyProfile(String uid) {
        return null;
    }

    @Override
    public UserResDto getUserProfile(String targetUid) {
        return null;
    }
}