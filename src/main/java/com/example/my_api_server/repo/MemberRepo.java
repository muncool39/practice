package com.example.my_api_server.repo;

import com.example.my_api_server.entity.Member;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.springframework.stereotype.Component;

// db 통신없이 간단하게 인메모리 DB 사용해 간단한 CRUD
// DAO: DB와 통신하는 객체
@Component
public class MemberRepo {
    Map<Long, Member> members = new HashMap<>();

    // 연산 (저장, 수정, 삭제, 조회)

    // 저장
    public Long saveMember(String email, String password) {
        long id = new Random().nextLong();
        Member member = Member.builder()
                .id(id)
                .email(email)
                .password(password)
                .build();
        members.put(id, member);
        return id;
    }

    // long 쓰면 안되나요? 질문...
    public Member findMember(Long id) {
        return members.get(id);
    }
}













