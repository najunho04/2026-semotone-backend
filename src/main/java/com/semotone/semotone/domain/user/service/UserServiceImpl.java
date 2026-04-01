package com.semotone.semotone.domain.user.service;


import com.semotone.semotone.domain.user.dto.UserCreateReqDto;
import com.semotone.semotone.domain.user.dto.UserLocationResDto;
import com.semotone.semotone.domain.user.dto.UserLocationUpdateReqDto;
import com.semotone.semotone.domain.user.dto.UserResDto;
import com.semotone.semotone.domain.user.entity.UserEntity;
import com.semotone.semotone.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

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
                .userId(uid)
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
        // 1. DB에서 UID로 유저 문서를 찾아옵니다. (없으면 에러 발생!)
        UserEntity userEntity = userRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("가입된 유저 정보를 찾을 수 없습니다."));

        // 2. 찾아온 Entity의 정보를 쏙쏙 뽑아서 클라이언트에게 보낼 DTO 박스에 담아줍니다.
        return UserResDto.builder()
                .uid(uid)
                .nickName(userEntity.getNickName())
                .gmail(userEntity.getGmail())
                .point(userEntity.getPoint())
                .acceptCount(userEntity.getAcceptCount())
                // 만약 프론트에서 내 위치 정보나 게시글 목록도 필요하다고 하면 여기서 추가로 담아주면 됩니다!
                // .latitude(userEntity.getLatitude())
                // .longitude(userEntity.getLongitude())
                .build();
    }

    @Override
    public UserResDto getUserProfile(String targetUid) {
        // 1. DB에서 UID로 유저 문서를 찾아옵니다. (없으면 에러 발생!)
        UserEntity userEntity = userRepository.findById(targetUid)
                .orElseThrow(() -> new RuntimeException("가입된 유저 정보를 찾을 수 없습니다."));

        // 2. 찾아온 Entity의 정보를 쏙쏙 뽑아서 클라이언트에게 보낼 DTO 박스에 담아줍니다.
        return UserResDto.builder()
                .uid(targetUid)
                .nickName(userEntity.getNickName())
                .gmail(userEntity.getGmail())
                .point(userEntity.getPoint())
                .acceptCount(userEntity.getAcceptCount())
                // 만약 프론트에서 내 위치 정보나 게시글 목록도 필요하다고 하면 여기서 추가로 담아주면 됩니다!
                // .latitude(userEntity.getLatitude())
                // .longitude(userEntity.getLongitude())
                .build();
    }

    @Override
    public void updateUserLocation(String uid, UserLocationUpdateReqDto dto) {
        // 복잡한 비즈니스 로직 없이, 바로 Repository로 위도/경도를 넘겨줍니다.
        userRepository.updateLocation(uid, dto.getLatitude(), dto.getLongitude());
    }

    @Override
    public void increasePointAndAcceptCount(String uid, int addPoint, int addAcceptCount) {
        // ★ 핵심: DB에서 유저 정보를 먼저 findById로 꺼내올 필요가 전혀 없습니다!
        // Firestore가 자체적으로 원자성(Atomicity)을 보장하며 값을 더해주기 때문에,
        // Repository로 증감분만 그대로 토스해 주면 끝납니다.

        userRepository.addPointAndAcceptCount(uid, addPoint, addAcceptCount);
    }

    @Override
    public void addPostIdToUser(String uid, String postId) {
        // 복잡한 계산 없이 바로 Repository로 던져서 동시성 문제 없이 배열에 추가합니다.
        userRepository.addPostIdToUser(uid, postId);
    }

    @Override
    public UserLocationResDto getMyLocation(String uid) {
        UserEntity userEntity = userRepository.findById(uid)
                // RuntimeException 대신 ResponseStatusException 사용 (404 상태 코드 지정)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "가입된 유저 정보를 찾을 수 없습니다."));

        return UserLocationResDto.builder()
                .uid(uid)
                .latitude(userEntity.getLatitude())
                .longitude(userEntity.getLongitude())
                .build();
    }

    @Override
    public List<UserResDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserResDto.builder()
                        .uid(user.getUserId())
                        .nickName(user.getNickName())
                        .gmail(user.getGmail())
                        .point(user.getPoint())
                        .acceptCount(user.getAcceptCount())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<UserLocationResDto> getAllUsersLocation() {
        return userRepository.findAll().stream()
                .map(user -> UserLocationResDto.builder()
                        .uid(user.getUserId())
                        .latitude(user.getLatitude())
                        .longitude(user.getLongitude())
                        .build())
                .collect(Collectors.toList());
    }
}