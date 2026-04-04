package com.example.my_api_server.service;

import com.example.my_api_server.common.OrderCreateFixture;
import com.example.my_api_server.entity.*;
import com.example.my_api_server.repo.MemberDBRepo;
import com.example.my_api_server.repo.OrderRepo;
import com.example.my_api_server.repo.ProductRepo;
import com.example.my_api_server.service.dto.OrderCreateDto;
import com.example.my_api_server.service.dto.OrderResponseDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


/**
 * OrderService 단위 테스트
 */


@ExtendWith(MockitoExtension.class) //Mockito 활성화
class OrderServiceUnitTest {

    @Mock   // 가짜 객체 생성
    ProductRepo productRepo;

    @Mock
    MemberDBRepo memberDBRepo;

    @Mock
    OrderRepo orderRepo;

    @InjectMocks    // 실제 서비스할 대상 클래스 (Mock객체 자동으로 주입받는다) 앞의 Mock객체를 주입받음
    OrderService orderService;

    InitData initData; // 초기 데이터용 클래스 객체

    OrderCreateDto orderCreateDto;

    @BeforeEach // 테스트 실행 전 실행
    public void init() {
        initData = new InitData(); // 객체 안에 메서드 만들어서 그것만 호출해도 됨

        initData.memberId = 1L;
        initData.productIds = List.of(1L, 2L);
        initData.counts = List.of(1L, 2L);

        initData.product1 = Product.builder()
                .productNumber("TEST1")
                .productName("티셔츠 1")
                .price(1000L)
                .stock(2L)
                .productType(ProductType.CLOTHES)
                .build();

        initData.product2 = Product.builder()
                .productNumber("TEST2")
                .productName("티셔츠 2")
                .price(2000L)
                .stock(4L)
                .productType(ProductType.CLOTHES)
                .build();

        initData.member = Member.builder()
                .email("test1@gmail.com")
                .password("1234")
                .build();

        orderCreateDto = new OrderCreateDto(
                initData.memberId,
                initData.productIds,
                initData.counts
        );
    }


    @Test
    @DisplayName("test1")
    public void test1() {
        // given - when에 필요한 데이터들 생성
        int a = 10;
        
        // when - 실제 수행할 메서드
        a++;

        // then - 결과 (검증)
        Assertions.assertThat(a).isEqualTo(11);
    }


    //TC1
    @Test
    @DisplayName("[HAPPY] 주문 요청이 정상적으로 잘 등록된다.")
    public void createOrderSuccess(){
        // given
        Long stockId = 1L;

        // DB와 통신하지 않도록 proxy처럼 임의로 실행
        when(productRepo.findAllById(initData.productIds))
                .thenReturn(List.of(initData.product1, initData.product2));
        when(memberDBRepo.findById(initData.memberId)).thenReturn(Optional.of(initData.member));

        // save 호출시 save된 order객체 반환되게
        when(orderRepo.save(any())).thenAnswer(invocation ->
                invocation.getArgument(0));


        // when (테스트할 메서드)
        OrderResponseDto dto = orderService.createOrder(orderCreateDto);

        // then
        ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepo).save(captor.capture());


        assertThat(dto.isSuccess()).isTrue(); // 성공 여부 검증
        assertThat(dto.getOrderStatus()).isEqualTo(OrderStatus.COMPLETED); // 주문 상태 검증
    }

    //TC2
    @Test
    @DisplayName("[Exception] 주문 요청시 재고가 부족하면 예외 처리가 정상 동작한다.")
    public void productStockValid(){
        // given
        // 공통적으로 데이터를 사용하지 않는 데이터 (재고 관련해서 조금 수정한다.)
        Long memberId = 1L;
        List<Long> productIds = List.of(1L, 2L);
        List<Long> counts = List.of(10L, 20L);

        Product product1 = Product.builder()
                .productNumber("TEST1")
                .productName("티셔츠 1")
                .productType(ProductType.CLOTHES)
                .price(1000L)
                .stock(1L)
                .build();

        Product product2 = Product.builder()
                .productNumber("TEST2")
                .productName("티셔츠 2")
                .productType(ProductType.CLOTHES)
                .price(2000L)
                .stock(2L)
                .build();

        Member member = Member.builder()
                .email("test1@gmail.com")
                .password("1234")
                .build();

        OrderCreateDto createDto = OrderCreateFixture.defaultDto(
                memberId, productIds, counts, LocalDateTime.now()
        );

        // DB와 통신하지 않도록 proxy처럼 임의로 실행
        when(productRepo.findAllById(productIds))
                .thenReturn(List.of(product1, product2));
        when(memberDBRepo.findById(memberId)).thenReturn(Optional.of(member));


        // when (테스트할 메서드)


        // then (값 검증)
        assertThatThrownBy(() -> orderService.createOrder(createDto))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("재고가 없어 주문 불가합니다.");

    }

    //TC3
    //@Test
    @DisplayName("[Exception] 주문 시간 날짜 오류 테스트")
    public void orderTimeException(){
        // given (when에 필요한 데이터 생성)

        // DB와 통신하지 않도록 proxy처럼 임의로 실행
        when(productRepo.findAllById(initData.productIds))
                .thenReturn(List.of(initData.product1, initData.product2));
        when(memberDBRepo.findById(initData.memberId)).thenReturn(Optional.of(initData.member));


        // when (테스트할 메서드)
        OrderResponseDto dto = orderService.createOrder(orderCreateDto);


        // then (값 검증)
        assertThat(dto).isNotNull();
    }

    /**
     * 테스트용 초기 데이터
     */
    public class InitData {
        public Long memberId;

        public List<Long> productIds;

        public List<Long> counts;

        public Product product1;

        public Product product2;

        public Member member;

    }

}


































