package com.semotone.semotone.domain.user.service;

import com.semotone.semotone.domain.user.dto.UserCreateReqDto;
import com.semotone.semotone.domain.user.dto.UserLocationResDto;
import com.semotone.semotone.domain.user.dto.UserLocationUpdateReqDto;
import com.semotone.semotone.domain.user.dto.UserResDto;
import com.semotone.semotone.domain.user.entity.UserEntity;
import com.semotone.semotone.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserResDto signUp(String uid, String email, UserCreateReqDto dto) {
        if (userRepository.existsById(uid)) {
            throw new RuntimeException("이미 가입한 사용자입니다.");
        }

        UserEntity userEntity = UserEntity.builder()
                .userId(uid)
                .nickName(dto.getNickName())
                .gmail(email)
                .point(1000)
                .acceptCount(0)
                .latitude(dto.getLatitude())
                .longitude(dto.getLongitude())
                .major(dto.getMajor())
                .school(dto.getSchool())
                .build();

        userRepository.save(uid, userEntity);

        return UserResDto.builder()
                .uid(uid)
                .nickName(userEntity.getNickName())
                .gmail(userEntity.getGmail())
                .point(userEntity.getPoint())
                .acceptCount(userEntity.getAcceptCount())
                .major(dto.getMajor())
                .school(dto.getSchool())
                .build();
    }

    @Override
    public UserResDto getMyProfile(String uid) {
        UserEntity userEntity = userRepository.findById(uid)
                .orElseThrow(() -> new RuntimeException("가입한 사용자 정보를 찾을 수 없습니다."));

        return UserResDto.builder()
                .uid(uid)
                .nickName(userEntity.getNickName())
                .gmail(userEntity.getGmail())
                .point(userEntity.getPoint())
                .acceptCount(userEntity.getAcceptCount())
                .major(userEntity.getMajor())
                .school(userEntity.getSchool())
                .build();
    }

    @Override
    public UserResDto getUserProfile(String targetUid) {
        UserEntity userEntity = userRepository.findById(targetUid)
                .orElseThrow(() -> new RuntimeException("가입한 사용자 정보를 찾을 수 없습니다."));

        return UserResDto.builder()
                .uid(targetUid)
                .nickName(userEntity.getNickName())
                .gmail(userEntity.getGmail())
                .point(userEntity.getPoint())
                .acceptCount(userEntity.getAcceptCount())
                .major(userEntity.getMajor())
                .school(userEntity.getSchool())
                .build();
    }

    @Override
    public void updateUserLocation(String uid, UserLocationUpdateReqDto dto) {
        userRepository.updateLocation(uid, dto.getLatitude(), dto.getLongitude());
    }

    @Override
    public void increasePointAndAcceptCount(String uid, int addPoint, int addAcceptCount) {
        userRepository.addPointAndAcceptCount(uid, addPoint, addAcceptCount);
    }

    @Override
    public void usePoints(String uid, int amount) {
        userRepository.usePoints(uid, amount);
    }

    @Override
    public void addPostIdToUser(String uid, String postId) {
        userRepository.addPostIdToUser(uid, postId);
    }

    @Override
    public void removePostIdFromUser(String uid, String postId) {
        userRepository.removePostIdFromUser(uid, postId);
    }

    @Override
    public UserLocationResDto getMyLocation(String uid) {
        UserEntity userEntity = userRepository.findById(uid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "가입한 사용자 정보를 찾을 수 없습니다."));

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
                        .major(user.getMajor())
                        .school(user.getSchool())
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
