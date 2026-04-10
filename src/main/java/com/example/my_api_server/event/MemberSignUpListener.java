package com.example.my_api_server.event;


import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@Slf4j
public class MemberSignUpListener {

    // asyncConfig 에서 설정한거 성능비교
//    @Async("cpuExecutor")
    @Async("ioExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void sendNotification(MemberSignUpEvent event) {
        // task-1이라는 새로운 스레드가 일한다.
        log.info("member ID: {}", event.getId());
        log.info("member Email: {}", event.getEmail());

        try {
            Thread.sleep(5000); // <- blocking
        } catch (InterruptedException e) {
            // 방법 2 실패한 것들을 DB에 저장했다가 나중에 한번에 모아서 발생 ->  트랜잭션 아웃박스 패턴 (실무!)
            throw new RuntimeException(e);
        }
        log.info("알림 전송 완료");
    }

    // 이벤트 받는 리스너
    // 아직 새로운 스레드 안붙임... 결국 시간은 안바뀐다
//    @Async // 다른 일꾼에게 맡긴다.
//    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
//    // @Retryable(maxRetries = 3) 방법 1 재시도 3번 -> 크게 의미가 없을 수 있음 지금 안되는걸 다시 바로 될까? 의미가...
//    public void sendNotification(MemberSignUpEvent event) {
//        // task-1이라는 새로운 스레드가 일한다.
//        log.info("member ID: {}", event.getId());
//        log.info("member Email: {}", event.getEmail());
//
//        try {
//            Thread.sleep(5000); // 가정
//        } catch (InterruptedException e) {
//            // 방법 2 실패한 것들을 DB에 저장했다가 나중에 한번에 모아서 발생 ->  트랜잭션 아웃박스 패턴 (실무!)
//            throw new RuntimeException(e);
//        }
//        log.info("알림 전송 완료");
//    }

}
