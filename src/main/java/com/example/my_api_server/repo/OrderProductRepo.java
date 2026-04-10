package com.example.my_api_server.repo;

import com.example.my_api_server.entity.OrderProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderProductRepo extends JpaRepository<OrderProduct, Long> {

    @Query("SELECT op FROM OrderProduct op WHERE op.order.id = :id")
    List<OrderProduct> findAllByOrderId(Long id);

}
