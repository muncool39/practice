package com.example.my_api_server.service.dto;

import com.example.my_api_server.entity.ProductType;
import lombok.Builder;

@Builder
public record ProductCreateDto(
        String productName,
        String productNumber,
        ProductType productType,
        Long price,
        Long stock
) {
}
