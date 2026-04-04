package com.example.my_api_server.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
@Getter
@Builder
public class Product {

    // 상품명 상품번호
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String productName;

    private String productNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductType productType;

    private Long price;

    private Long stock;

    @Version
    private Long version;

    // 필요한 것만 바꿀 수 있도록
    public void changeProductName(String changeProductName) {
        this.productName = changeProductName;
    }

    public void increaseStock(Long addStock) {
        this.stock += addStock;
    }

    public void decreaseStock(Long subStock) {
        this.stock -= subStock;
    }

    public void buyProductWithStock(Long orderCount) {
        if (this.stock - orderCount < 0) {
            throw new RuntimeException("재고가 없어 주문 불가합니다.");
        }

        this.decreaseStock(orderCount);
    }

}

















