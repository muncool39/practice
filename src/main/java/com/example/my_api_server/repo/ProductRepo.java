package com.example.my_api_server.repo;

import com.example.my_api_server.entity.Product;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepo extends JpaRepository<Product, Long> {

    // FOR NO KEY UPDATE 레코드락 (PG), FOR UPDATE (MySql)
    // 동일한 레코드에 대해 동시 업데이트를 방지한다 (PG기준)
    // 그래서 트랜잭션이 동일한 로우에 대해 최신 스냅샷을 읽기때문에 동시성이슈가 없어지게 된다 (정합성보장)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Product p WHERE p.id IN :ids ORDER BY p.id")
    // JPQL: 자바 객체로 쿼리 구성하는 방법
    List<Product> findAllByIdsWithXLock(List<Long> ids);


}
