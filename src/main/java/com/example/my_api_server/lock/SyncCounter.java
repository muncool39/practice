package com.example.my_api_server.lock;


import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class SyncCounter {

    private int count = 0; // 해당 공유영역(Heap) 값을 동시에 사용해보자

    public static void main(String[] args) {
        List<Thread> threads = new ArrayList<>();
        int threadCount = 3;
        SyncCounter counter = new SyncCounter();

        // 스레드 생성
        for (int i = 0; i < threadCount; i++) {
            Thread thread = new Thread(counter::increaseCount);
            thread.start();
            threads.add(thread);
        }

        // 스레드가 일이 다 끝날때까지 기다림
        threads.forEach(t -> {
            try {
                t.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        log.info("기대값 : {}", threadCount);
        log.info("실제값 : {}", counter.getCount());

    }

    // 이렇게 상단에 synchronized하면 스레드가 순서대로 처리된다 (메서드단위 락) (return하면 반납)
    // 문제때문에 안씀 쓰지마!
    private synchronized void increaseCount() {
        //스레드 1번이 들어오면서 락 획득
        State state = Thread.currentThread().getState();
        log.info("state = {}", state.toString());
//        해당 범위만 락을 얻게 된다.
//        synchronized (this) { // 락으로 순서 제어
//            log.info("락 얻는 부분 state = {}", state.toString());
//            count++;
//        }

        //스레드 1번의 락 반환
        log.info("state = {}", state.toString());
    }


}

















