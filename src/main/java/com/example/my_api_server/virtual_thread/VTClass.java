package com.example.my_api_server.virtual_thread;

import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

@Slf4j
public class VTClass {

    static final int TASK_COUNT = 1000; // 200이상부터 속도 차이들이... 늘릴수록 더 비교된다. (io)
    // 근데 io작업은 차이가 점점 나는데 cpu는 차이가 줄어든다...

    static final Duration IO_DURATION = Duration.ofSeconds(1); // io 작업시간

    static void main() {

//        log.info("[i/o] 플랫폼 스레드 시작");
//        ioRun(Executors.newFixedThreadPool(200)); // 플랫폼 스레드 N개 생성 <- 미리 만듦
//
//        log.info("[i/o] 가상 스레드 시작");
//        ioRun(Executors.newVirtualThreadPerTaskExecutor()); // 가상 스레드 필요한만큼 생성 <- 필요할 떄 만듦
//
//        // 2MB * 200 메모리 고정해서 사용
//        log.info("[cpu] 플랫폼 스레드 시작");
//        cpuRun(Executors.newFixedThreadPool(200));
//
//        // 1KB * TASKCOUNT 메모리 공간 커지는데 얜 힙공간에 저장됨 <- 나중에 stw 현상이 길겠죠
//        log.info("[cpu] 가상 스레드 시작");
//        cpuRun(Executors.newVirtualThreadPerTaskExecutor());

        // 가상스레드는 힙에 저장되는데 결국 jvm 힙 메모리가 더 많이 사용되고 GC 시간이 많이걸리고...
        // 결국 사용자 많아질수록 cpu 연산의 차이가 크고 비효율적
        // io작업은 메모리 많이 사용하더라도 빠르다..
        // 즉 가상스레드는 만능은 아니고 i/o 작업이 많은 상황에서 효율적


//        // 피닝 테스트 (synchronized)
//        log.info("[i/o] 플랫폼 스레드 피닝 테스트 시작");
//        ioRunPinning(Executors.newFixedThreadPool(200));
//
//        log.info("[i/o] 가상 스레드 피닝 테스트 시작");
//        ioRunPinning(Executors.newVirtualThreadPerTaskExecutor());


        // 피닝 테스트 (ReentrantLock)
        log.info("[i/o] 플랫폼 스레드 피닝 테스트 2 시작");
        ioRunPinningRL(Executors.newFixedThreadPool(200));

        log.info("[i/o] 가상 스레드 피닝 테스트 2 시작");
        ioRunPinningRL(Executors.newVirtualThreadPerTaskExecutor());

        // 피닝... 나도모르게 sync block 을 활용하면 가상 스레드의 장점을 잃는다
        // -> 유저 레벨 수준의 컨텍스트 스위칭을 할 수 없게 됨
        // (가상 스레드는 유저 수준 컨텍스트 스위칭으로 성능을 올린거니까)
        // -> 최신 자바를 쓰던가 (이전 버전은 sync 쓰니까) (+mysql도 내부에 있어서 이것도 신경써야됨)
        // ReentrantLock 는 커널로 진입하지 않아서 다른 스레드도 일 가능함
    }

    public static void ioRun(ExecutorService es) {
        Instant start = Instant.now(); // 실행시간 측정

        // io bound
        try(es) {
            IntStream.range(0, TASK_COUNT).forEach(idx -> {
                es.submit(() -> {
                    try {
                        // 기존 플랫폼스레드는 i/o만나면 블락되어 일을 쉰다
                        // 가상스레드는 i/o만나면 pt는 일을 하고 가상스레드 하나가 언마운트되면서 다른 가상스레드가 파킹되어 일함
                        // pt -> 다같이 쉼, vt -> pt는 일하고 vt가 일하다옴
                        // = 가상 스레드는 i/o를 만나면 언마운트되고 다른 가상스레드가 일을 할 수 있게 됨
                        Thread.sleep(IO_DURATION); // 실제 외부 API 연동 및 DB 연동 (i/o발생) 코드 가정
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            });
        } // try-resource 자동으로 리소스 해제하는 문법 (es.close())

        Instant end = Instant.now(); // 실행시간 측정
        System.out.printf("작업 완료 시간 %d ms%n", Duration.between(start, end).toMillis());

    }

    // 어? 그럼 가상스레드만 사용하면 되겠네?
    // 근데.. cpu 작업은 차이가 갈수록...

    public static void cpuRun(ExecutorService es) {
        Instant start = Instant.now(); // 실행시간 측정

        // cpu bound
        try(es) {
            IntStream.range(0, TASK_COUNT).forEach(idx -> {
                es.submit(() -> {
                    // cpu 연산이 많다 -> cpu bound
                    for (int i = 0; i < 1000000; i++) {
                        int a = 1;
                        int b = 2;
                        int c = a + b;
                    }
                });
            });
        } // try-resource 자동으로 리소스 해제하는 문법 (es.close())

        Instant end = Instant.now(); // 실행시간 측정
        System.out.printf("작업 완료 시간 %d ms%n", Duration.between(start, end).toMillis());

    }

    // 내부적으로 락을 사용해야 한다면?
    public static void ioRunPinning(ExecutorService es) {
        Instant start = Instant.now(); // 실행시간 측정

        try(es) {
            IntStream.range(0, TASK_COUNT).forEach(idx -> {
                es.submit(() -> {
                    // 락 사용한다면?
                    // syn : 커널의 세마포어, 뮤텍스 객체로 동시성 제어
                    // 그래서 이거 쓰면 시스템콜해서 플랫폼 스레드 1이 일 못함
                    // -> 연결된 가상스레드들도 일 못함 (가상 스레드는 플랫폼 스레드 위에서 동작하니까)
                    // 가상스레드는 syn 만나면 일을 못하니 장점이 사라져버림
                    // 그레서 공식 문서 -> 가상스레드 사용할 때는 syn 말고 ReentrantLock 사용하라고 함 (밑에 메서드)

                    //synchronized (new Object()) { // TASK_COUNT 250일때 결과 pt 2010, vt 1017
                    synchronized (es) { // TASK_COUNT 10일때 결과 pt 10038, vt 10052 -> 가상스레드의 장점이 없어짐
                        try {
                            Thread.sleep(IO_DURATION);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                });
            });
        }

        Instant end = Instant.now(); // 실행시간 측정
        System.out.printf("작업 완료 시간 %d ms%n", Duration.between(start, end).toMillis());

    }


    // ReentrantLock
    public static void ioRunPinningRL(ExecutorService es) {
        Instant start = Instant.now(); // 실행시간 측정

        try(es) {
            IntStream.range(0, TASK_COUNT).forEach(idx -> {
                es.submit(() -> {
                    ReentrantLock lock = new ReentrantLock();
                    lock.lock();
                    try {
                        Thread.sleep(IO_DURATION);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        lock.unlock();
                    }
                });
            });
        }

        Instant end = Instant.now();
        System.out.printf("작업 완료 시간 %d ms%n", Duration.between(start, end).toMillis());

    }

}
