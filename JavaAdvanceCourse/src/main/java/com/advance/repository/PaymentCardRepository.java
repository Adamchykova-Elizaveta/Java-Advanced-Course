package com.advance.repository;

import com.advance.entity.PaymentCard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentCardRepository extends JpaRepository<PaymentCard, Long>, JpaSpecificationExecutor<PaymentCard> {

    List<PaymentCard> findAllByUserId(Long userId);
    Page<PaymentCard> findAllByUserId(Long userId, Pageable pageable);
    int countByUserId(Long userId);

    @Query("SELECT pc FROM PaymentCard pc WHERE pc.user.id = :userId AND pc.active = true")
    List<PaymentCard> findActiveCardsByUserId(@Param("userId") Long userId);

    @Modifying
    @Query(value = "UPDATE payment_cards SET active = :active WHERE id = :id", nativeQuery = true)
    void updateActiveStatus(@Param("id") Long id, @Param("active") Boolean active);
}