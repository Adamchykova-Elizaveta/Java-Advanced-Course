package com.advance.order.repository;

import com.advance.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findAllByDeletedFalse();

    Optional<Order> findByIdAndDeletedFalse(Long id);

    @Query("SELECT o FROM Order o WHERE o.userId = :userId AND o.deleted = false")
    List<Order> findAllByUserIdAndDeletedFalse(@Param("userId") Long userId);
}