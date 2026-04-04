package com.example.my_api_server.common;

import com.example.my_api_server.service.dto.OrderCreateDto;

import java.time.LocalDateTime;
import java.util.List;

public class OrderCreateFixture {

    public static OrderCreateDto defaultDto(
            Long memberId,
            List<Long> productIds,
            List<Long> count,
            LocalDateTime orderTime
    ) {
        return new OrderCreateDto(memberId, productIds, count, orderTime);
    }
}
