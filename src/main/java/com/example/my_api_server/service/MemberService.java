package com.example.my_api_server.service;

import com.example.my_api_server.entity.Member;
import com.example.my_api_server.repo.MemberRepo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {

    private final MemberRepo memberRepo;

    /***
     * 회원가입 - 회원가입 후 알림 전송
     * @param email
     * @param password
     * @return
     */
    public Long signup(String email, String password) {
        // 회원 저장 후 알림을 전송한다
        Long memberId = memberRepo.saveMember(email, password);
        log.info("회원가입한 member ID = {}", memberId);

        // 알림 전송
        sendNotification();

        return memberId;
    }

    public void sendNotification() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("알림 전송 완료");
    }

    public Member findMember(Long id) {
        return memberRepo.findMember(id);
    }

}




















