package com.example.my_api_server.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {

    // i/0 bound
    @Bean("ioExecutor")
    public ExecutorService ioExecutor() {
        return Executors.newVirtualThreadPerTaskExecutor();
    }

    // cpu bound
    @Bean("cpuExecutor")
    public ExecutorService cpuExecutor() {
        // cpu 코어 개수 확인 (하이퍼스레딩)
        // 인텔 사용하면 코어 * 2
        // 멕 인텔칩 <- 하이퍼스레딩 o, 애플칩 <- 하이퍼스레딩 x
        int coreCount = Runtime.getRuntime().availableProcessors();
        // 스레드 개수는 막 넣으면 안된다 (컨텍스트 스위칭 비용 때문에)
        // 그래서 서비스 규모 등을 파악하고 잘 계산해서 넣어야 함
        return Executors.newFixedThreadPool(coreCount);
    }
}




















