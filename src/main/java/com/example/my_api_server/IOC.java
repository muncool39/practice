package com.example.my_api_server;


import org.springframework.stereotype.Component;

// 실제 Spring에 빈(객체)으로 등록
// IOC 컨테이너에 등록, 단 하나만 생성해 재사용 <- 싱글톤 패턴
@Component
public class IOC {


    public  void fun1() {
        System.out.println("func1 실행");
    }

    public static void main(String[] args) {
        // 객체 생성
        // 메모리 (RAM) - JVM의 Heap 메모리에 사용됨 -OOM 문제

        // IOC란? -> Spring아 우리가 IOC 객체를 만들테니 하나로 만들어 재사용하게 해줘
        // = 개발자가 직접 만들지 않고 소프트웨어가 관리, 필요할 때 주입 (DI)
        IOC ioc = new IOC();

        // 객체의 메서드 호출
        ioc.fun1();;
    }
}
