package com.example.my_api_server.lock;


import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Getter
@Slf4j
public class ReentrantCounter {

    private final ReentrantLock lock = new ReentrantLock();
    private int count = 0; // 해당 공유영역(Heap) 값을 동시에 사용해보자

    public static void main(String[] args) {
        List<Thread> threads = new ArrayList<>();
        int threadCount = 3;
        ReentrantCounter counter = new ReentrantCounter();

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


    // 락 획득과 반환이 개발자가 원하는 시점에!!
    private void increaseCount() {
        this.lock.lock();

        try {
            if (this.lock.tryLock(3, TimeUnit.SECONDS)) {
                try {
                    log.info("락 획득 후 연산 작업 시작");
                    this.count++;
                    Thread.sleep(4000);
                } finally {
                    this.lock.unlock();
                }
            } else {
                //3초안에 락 획득 못하면
                log.info("3초안에 락 획득 못함");
            }
        } catch (InterruptedException e) {
            log.info("작업 중단");
            throw new RuntimeException(e);
        }


    }


}

















