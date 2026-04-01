package com.example.my_api_server.controller;


import com.example.my_api_server.entity.Member;
import com.example.my_api_server.repo.MemberDBRepo;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberPointService {

    private final MemberDBRepo memberDBRepo;


    // REQUIRES_NEW - 트랜잭션을 새로 만든다
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void changeAllUserData() {
        List<Member> members = memberDBRepo.findAll();
        // 값 바꿨다 가정
    }

    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public void supportTxTest() {
        // DB 사용하지 않는 단순 자바 코드 실행하거나 혹은 readOnly=true로 최적화된 읽기 할 때 가~~~~끔사용
        // (사실 잘 안써...)
        memberDBRepo.findAll();
    }

    // timeout = 트랜잭션의 총 실행시간을 제한, 그 시간 검게 걸리면 예외 발생
    // 상위트랜잭션도 바꿔줘야댐 근데 REQUIRES_NEW로 하면 상위 안바꾸고 여기만 timeout 지정해도 오류로 잘 됨
    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 2) //default: -1 = 무제한으로 기다리겠다
    public void timeout() {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        memberDBRepo.findAll();
    }
}
