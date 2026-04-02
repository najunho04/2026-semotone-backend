package com.semotone.semotone.domain.user.service;

import com.semotone.semotone.domain.user.dto.UserCreateReqDto;
import com.semotone.semotone.domain.user.dto.UserLocationResDto;
import com.semotone.semotone.domain.user.dto.UserLocationUpdateReqDto;
import com.semotone.semotone.domain.user.dto.UserResDto;

import java.util.List;

public interface UserService {
    UserResDto signUp(String uid, String email, UserCreateReqDto dto);

    UserResDto getMyProfile(String uid);

    UserResDto getUserProfile(String targetUid);

    void updateUserLocation(String uid, UserLocationUpdateReqDto dto);

    void increasePointAndAcceptCount(String uid, int addPoint, int addAcceptCount);

    void usePoints(String uid, int amount);

    void addPostIdToUser(String uid, String postId);

    void removePostIdFromUser(String uid, String postId);

    UserLocationResDto getMyLocation(String uid);

    List<UserResDto> getAllUsers();

    List<UserLocationResDto> getAllUsersLocation();
}
