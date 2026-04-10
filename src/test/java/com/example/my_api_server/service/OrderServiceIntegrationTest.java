package com.example.my_api_server.service;


import com.example.my_api_server.common.MemberFixture;
import com.example.my_api_server.common.ProductFixture;
import com.example.my_api_server.config.TestContainerConfig;
import com.example.my_api_server.entity.Member;
import com.example.my_api_server.entity.OrderProduct;
import com.example.my_api_server.entity.Product;
import com.example.my_api_server.repo.MemberDBRepo;
import com.example.my_api_server.repo.OrderProductRepo;
import com.example.my_api_server.repo.OrderRepo;
import com.example.my_api_server.repo.ProductRepo;
import com.example.my_api_server.service.dto.OrderCreateDto;
import com.example.my_api_server.service.dto.OrderResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest // Spring DI를 통해 빈(Bean) 주입헤주는 어노테이션 (Run해주는거랑 비슷)
@Import(TestContainerConfig.class)
@ActiveProfiles("test") // yaml 파일 어떤거 읽을지
public class OrderServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepo orderRepo;

    @Autowired
    private ProductRepo productRepo;

    @Autowired
    private MemberDBRepo memberDBRepo;

    @Autowired
    private OrderProductRepo orderProductRepo;

    @BeforeEach
    public void setUp() {
        orderProductRepo.deleteAllInBatch();
        productRepo.deleteAllInBatch();
        orderRepo.deleteAllInBatch();
        memberDBRepo.deleteAllInBatch();
    }
    
    private Member getSavedMember(String password) {
        return memberDBRepo.save(MemberFixture
                .defaultMember()
                .password(password)
                .build()
        );
    }

    private List<Product> getProducts() {
        return productRepo.saveAll(ProductFixture.defaultProducts());
    }

    private List<Long> getProductIds(List<Product> products) {
        return products.stream()
                .map(Product::getId)
                .toList();
    }

    @Nested()
    @DisplayName("주문 생성 TC")
    class OrderCreateTest {
        @Test
        @DisplayName("주문 생성 시 DB에 저장되고 주문 시간이 Null이 아니다.")
        public void createOrderPersistAndReturn(){
            // given
            List<Long> counts = List.of(1L, 2L);
            Member savedMember = getSavedMember("1234");
            List<Product> products = getProducts();
            List<Long> productIds = getProductIds(products);
            OrderCreateDto createDto = new OrderCreateDto(savedMember.getId(), productIds, counts);

            // when
            OrderResponseDto resDto = orderService.createOrder(createDto);

            // then
            assertThat(resDto.getOrderCompletedTime()).isNotNull();
        }

        @Test
        @DisplayName("주문 생성 시 재고가 정상적으로 차감이 된다.")
        public void createOrderStockDecreaseSuccess(){
            // given
            List<Long> counts = List.of(1L, 2L);
            Member savedMember = getSavedMember("1234");
            List<Product> products = getProducts();
            List<Long> productIds = getProductIds(products);
            OrderCreateDto createDto = new OrderCreateDto(savedMember.getId(), productIds, counts);

            // when
            OrderResponseDto resDto = orderService.createOrder(createDto);

            // then
            List<Product> resultProducts = productRepo.findAllById(productIds);

            // 처음재고 - 주문재고 = 현재재고 (<- 가 잘 반영되었는지 확인) (주문 재고 반영 확인)
            for (int i=0; i<products.size(); i++) {
                Product beforProduct = products.get(i);
                Product nowProduct = resultProducts.get(i);
                Long orderStock = counts.get(i);

                assertThat(beforProduct.getStock() - orderStock)
                        .isEqualTo(nowProduct.getStock());
            }
        }

        @Test
        @DisplayName("주문 생성 시 재고가 부족하면 예외가 정상적으로 반횐된다.")
        public void createOrderStockValidation(){
            // given
            List<Long> counts = List.of(10L, 20L);
            Member savedMember = getSavedMember("1234");
            List<Product> products = getProducts();
            List<Long> productIds = getProductIds(products);

            OrderCreateDto createDto = new OrderCreateDto(savedMember.getId(), productIds, counts);

            // when

            // then
            assertThatThrownBy(() -> orderService.createOrder(createDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("재고가 없어 주문 불가합니다.");
        }

        @Test
        @DisplayName("주문 생성 시 주문 상품이 정상적으로 생성된다.")
        public void createOrderAfterStockValidation(){
            // given
            List<Long> counts = List.of(1L, 2L);
            Member savedMember = getSavedMember("1234");
            List<Product> products = getProducts();
            List<Long> productIds = getProductIds(products);
            OrderCreateDto createDto = new OrderCreateDto(savedMember.getId(), productIds, counts);

            // when
            OrderResponseDto resDto = orderService.createOrder(createDto);

            // then
            List<OrderProduct> result = orderProductRepo.findAllByOrderId(resDto.getOrderId());

            assertThat(result.size()).isEqualTo(products.size());

            for(int i=0; i<products.size(); i++) {
                assertThat(result.get(i).getProduct().getId()).isEqualTo(productIds.get(i));
                assertThat(result.get(i).getNumber()).isEqualTo(counts.get(i));
            }
        }
    }

    @Nested()
    @DisplayName("주문과 연관된 도메인 예외 TC")
    class OrderRelatedExceptionTest {
        @Test
        @DisplayName("주문 시 회원이 존재하지 않으면 예외가 발생한다.")
        public void validateMemberWhenCreateOrder(){
            // given
            List<Long> counts = List.of(1L, 2L);
            List<Product> products = getProducts();
            List<Long> productIds = getProductIds(products);

            OrderCreateDto createDto = new OrderCreateDto(1234L, productIds, counts);

            // when + then
            assertThatThrownBy(() -> orderService.createOrder(createDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("회원이 존재하지 않습니다.");

        }


        @Test
        @DisplayName("주문 시 상품이 존재하지 않으면 예외가 발생한다.")
        public void validateProductWhenCreateOrder(){
            // given
            List<Long> counts = List.of(1L, 2L);
            List<Long> productIds = List.of(100L, 200L);

            Member savedMember = getSavedMember("1234");
            OrderCreateDto createDto = new OrderCreateDto(savedMember.getId(), productIds, counts);

            // when + then
            assertThatThrownBy(() -> orderService.createOrder(createDto))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("상품이 존재하지 않습니다.");
        }

    }


}






















