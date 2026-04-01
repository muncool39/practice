package com.example.my_api_server.service;


import com.example.my_api_server.entity.Product;
import com.example.my_api_server.repo.ProductRepo;
import com.example.my_api_server.service.dto.ProductCreateDto;
import com.example.my_api_server.service.dto.ProductResDto;
import com.example.my_api_server.service.dto.ProductUpdateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepo productRepo;


    @Transactional
    public ProductResDto createProduct(ProductCreateDto dto) {
        Product product = Product.builder()
                .productName(dto.productName())
                .productType(dto.productType())
                .productNumber(dto.productNumber())
                .price(dto.price())
                .stock(dto.stock())
                .build();
        Product savedProduct = productRepo.save(product);
        return ProductResDto.builder()
                .productNumber(savedProduct.getProductNumber())
                .stock(savedProduct.getStock())
                .price(savedProduct.getPrice())
                .build();
    }

    public ProductResDto findProduct(Long productId) {
        Product product = productRepo.findById(productId).orElseThrow();
        return ProductResDto.builder()
                .productNumber(product.getProductName())
                .stock(product.getStock())
                .price(product.getPrice())
                .build();
    }

    @Transactional
    public ProductResDto updateProduct(ProductUpdateDto dto) {
        Product product = productRepo.findById(dto.productId()).orElseThrow();
        product.changeProductName(dto.changeProductName());
        product.increaseStock(dto.changeStock()); // 수량 증가
        return ProductResDto.builder()
                .productNumber(product.getProductName())
                .stock(product.getStock())
                .price(product.getPrice())
                .build();

    }
}



























