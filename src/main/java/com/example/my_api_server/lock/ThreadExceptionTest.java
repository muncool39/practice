package com.example.my_api_server.lock;


import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadExceptionTest {

    private int count = 0;

    // Exception : unable to create native thread: possibly out of memory or process/resource limits reached
    static void main() {
        ThreadExceptionTest t = new ThreadExceptionTest();
        int threadCount = 10000;
        // 기존 Counter는 new Thread를 1000개 생성해서 돌려도 문제 없이 동작하는데,
        // 왜 newFixedThreadPool로 1000개 생성해서 돌리면 문제가 발생하는 걸까?
        // new Thread 는 작업이 끝나면 반납하는데,
        // newFixedThreadPool은 n개를 커널로부터 생성해도 자원을 반납하지 않기 때문
        // 그래서 미리 만들어서 재사용하는 경우에 사용된다 (각각 장단점이 있음)
        ExecutorService es = Executors.newFixedThreadPool(threadCount); // n개의 플랫폼스레드 생성

        for (int i = 0; i < threadCount; i++) {
            es.submit(t::increase);
        }
        es.shutdown();
        System.out.println("실행완료!");
    }

    public void increase() {
        count++;
    }
}
