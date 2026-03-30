package com.semotone.semotone.domain.user.entity;

import com.google.cloud.firestore.GeoPoint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor // Firestore 역직렬화에 필수!
@AllArgsConstructor
@Builder
public class UserEntity {
    // uid는 문서의 ID(Key)로 사용되므로 내부에 필드로 중복 저장하지 않아도 되지만,
    // 필요에 따라 필드로 가지고 있어도 무방합니다.
    private String nickName;
    private String gmail;
    private int point;
    private List<String> myPosts; // 작성한 게시글 ID 리스트
    private int acceptCount;
    private double latitude;
    private double longitude;


    // chatLog는 서브 컬렉션이므로 Entity 필드에 포함하지 않고 DB 참조로만 사용합니다.
}