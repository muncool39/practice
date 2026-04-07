package com.example.my_api_server.service;


import com.example.my_api_server.entity.Member;
import com.example.my_api_server.entity.Order;
import com.example.my_api_server.entity.OrderProduct;
import com.example.my_api_server.entity.OrderStatus;
import com.example.my_api_server.entity.Product;
import com.example.my_api_server.repo.MemberDBRepo;
import com.example.my_api_server.repo.OrderRepo;
import com.example.my_api_server.repo.ProductRepo;
import com.example.my_api_server.service.dto.OrderCreateDto;
import com.example.my_api_server.service.dto.OrderResponseDto;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepo orderRepo;
    private final ProductRepo productRepo;
    private final MemberDBRepo memberRepo;

    //주문 생성
    @Transactional
    public OrderResponseDto createOrder(OrderCreateDto dto) {
        Member member = memberRepo.findById(dto.memberId())
                .orElseThrow(() -> new RuntimeException("회원이 존재하지 않습니다."));

        Order order = Order.createOrder(member, dto.orderTime());
        List<Product> products = productRepo.findAllById(dto.productId());

        if(dto.productId().size() != products.size()) {
            throw new RuntimeException("상품이 존재하지 않습니다.");
        }

        List<OrderProduct> orderProducts = IntStream.range(0, dto.count().size())
                .mapToObj(idx -> {
                    Product product = products.get(idx);
                    Long orderCount = dto.count().get(idx);
                    product.buyProductWithStock(orderCount);
                    return order.createOrderProduct(orderCount, product);
                })
                .toList();

        order.addOrderProducts(orderProducts);
        Order savedOrder = orderRepo.save(order);

        return OrderResponseDto.of(
                savedOrder.getOrderTime(), OrderStatus.COMPLETED, true
        );
    }

    // 주문 수정 - 주문 확정으로 바꾸는 메서드 만들어보기!


    @Transactional(readOnly = true)
    public OrderResponseDto findOrder(Long orderId) {
        Order order = orderRepo.findById(orderId).orElseThrow();
        return OrderResponseDto.of(
                order.getOrderTime(), order.getOrderStatus(), true
        );
    }

    // 낙관락 적용 예시
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Retryable(includes = ObjectOptimisticLockingFailureException.class, maxRetries = 3) // 재시도 3번
    public OrderResponseDto createOrderOptLock(OrderCreateDto dto) {
        log.info("@Retryable 테스트");
        Member member = memberRepo.findById(dto.memberId()).orElseThrow();
        Order order = Order.builder()
                .buyer(member)
                .orderStatus(OrderStatus.PENDING)
                .orderTime(LocalDateTime.now())
                .build();
        List<Product> products = productRepo.findAllById(dto.productId());

        List<OrderProduct> orderProducts = IntStream.range(0, dto.count().size())
                .mapToObj(idx -> {
                    Product product = products.get(idx);
                    if (product.getStock() - dto.count().get(idx) < 0) {
                        throw new RuntimeException("재고가 없어 주문 불가합니다");
                    }
                    product.decreaseStock(dto.count().get(idx));

                    return OrderProduct.builder()
                            .order(order).number(dto.count().get(idx))
                            .product(products.get(idx))
                            .build();
                }).toList();

        order.addOrderProducts(orderProducts);
        Order savedOrder = orderRepo.save(order);

        return OrderResponseDto.of(savedOrder.getOrderTime(), OrderStatus.COMPLETED, true);
    }

    // 비관적 락 예시
    @Transactional
    public OrderResponseDto createOrderPLock(OrderCreateDto dto) {
        Member member = memberRepo.findById(dto.memberId()).orElseThrow();
        Order order = Order.builder()
                .buyer(member)
                .orderStatus(OrderStatus.PENDING)
                .orderTime(LocalDateTime.now())
                .build();

        // **** 여기 락 적용 : FOR no update lock
        // products 에 대해 x-lock이 걸리게 된다
        // 그래서 다른 트랜잭션은 이전 트랜잭션을 기다린다 끝나고 x-lock 얻어서 연산한다 (java의 sync와 비슷)
        List<Product> products = productRepo.findAllByIdsWithXLock(dto.productId());

        List<OrderProduct> orderProducts = IntStream.range(0, dto.count().size())
                .mapToObj(idx -> {
                    Product product = products.get(idx);
                    if (product.getStock() - dto.count().get(idx) < 0) {
                        throw new RuntimeException("재고가 없어 주문 불가합니다");
                    }
                    product.decreaseStock(dto.count().get(idx));

                    return OrderProduct.builder()
                            .order(order).number(dto.count().get(idx))
                            .product(products.get(idx))
                            .build();
                }).toList();
        order.addOrderProducts(orderProducts);
        Order savedOrder = orderRepo.save(order);

        return OrderResponseDto.of(savedOrder.getOrderTime(), OrderStatus.COMPLETED, true);
    }
}
















