package com.example.my_api_server.common;

import com.example.my_api_server.entity.Member;


// 공통으로 사용하는 멤버 생성 클래스
public class MemberFixture {

    // 이메일, 비밀번호 (이메일은 고정된 값을 사용한다고 가정하면)
    // 정적 팩토리 메서드 (+빌더패턴)
    public static Member.MemberBuilder defaultMember() {
        return Member.builder()
                .email("test1@gmail.com");
    }
}




























