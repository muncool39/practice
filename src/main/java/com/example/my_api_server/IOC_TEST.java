package com.example.my_api_server;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor // 생성자 주입 방식의 어노테이션
public class IOC_TEST {

    // 불변성 보장
    private final IOC ioc;

    // DI 방법
    /* 1. 필드 주입 (잘 안씀)
    @Autowired
    private IOC ioc2;
     */

    /*
    public IOC setIoc(Ioc) {
        ioc2 = ioc;
        return ioc2;
    }
     */

    /* 3. 생성자 주입 (생성할 때 자동으로 주입, 주로 사용됨) <- @RequiredArgsConstructor
    public void IOC(IOC ioc) {
        ioc = ioc
    }
     */

    @GetMapping
    public void iocTest() {
        ioc.fun1();
    }
}
