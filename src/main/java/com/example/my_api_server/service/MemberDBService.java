package com.example.my_api_server.service;

import com.example.my_api_server.controller.MemberPointService;
import com.example.my_api_server.entity.Member;
import com.example.my_api_server.event.MemberSignUpEvent;
import com.example.my_api_server.repo.MemberDBRepo;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberDBService {
    private final MemberDBRepo memberDBRepo;
    private final MemberPointService pointService;
    private final ApplicationEventPublisher publisher; //이벤트를 보내줄 퍼블리셔

    /**
     * 1. @Transactional은 AOP로 돌아가서 begin tran() commit() 2. DB에 commit 명령어가 실핼되어야 테이블에 반영됨 3. Jpa의 구현체인 하이버네이트와 엔티티매니저
     * JDBC Driver <-> DB 과정을 자동으로 해줌
     */
    //회원 저장
    // @Transactional(rollbackFor = IOException.class) // 원래 런타임에러만 롤백하는데 해당 클래스도 예외로 롤백해주겠다
    @Transactional
    public Long signUp(String email, String password) throws IOException {
        Member member = Member.builder()
                .email(email)
                .password(password)
                .build();

        //저장
        Member savedMember = memberDBRepo.save(member);

        //sendNotification();
        //이벤트 발송
        publisher.publishEvent(new MemberSignUpEvent(savedMember.getId(), savedMember.getEmail()));

        //pointService.changeAllUserData();

        //throw new IOException("외부 API 호출하다가 I/O 예외가 터짐");
        //DB에 저장하다가 뭔가 오류가 발생해서 예외가 터짐(Runtime 예외)
        //throw new RuntimeException("DB에 저장하다가 뭔가 오류가 발생해서 예외가 터짐");

        return savedMember.getId();
    }


    // 이메일, 알림 가정
    // 비동기를 사용할 경우 (@Async) 재시도를 어떻게 할건가?
    public void sendNotification() {
        try {
            Thread.sleep(5000); // 가정
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("알림 전송 완료");
    }

    // TX 테스트 메서드
    @Transactional(propagation = Propagation.REQUIRED) // REQUIRED: 기본값 (가장 일반적)
    public void tx1() {
        List<Member> members = memberDBRepo.findAll();
        members.forEach((m) -> {
            log.info("member id = {}", m.getId());
            log.info("member email = {}", m.getEmail());
        });

        pointService.changeAllUserData();

        pointService.timeout();
    }

}

















