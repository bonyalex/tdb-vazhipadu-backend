package com.example.vazhipadu.repository;

import com.example.vazhipadu.entity.DevoteeOrder;
import com.example.vazhipadu.entity.OrderStatus;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DevoteeOrderRepository
        extends JpaRepository<DevoteeOrder, Long>, JpaSpecificationExecutor<DevoteeOrder> {

    @Query("select coalesce(sum(l.quantity), 0) from DevoteeOrder o join o.lines l"
            + " where l.offering.id = :offeringId and o.orderDate = :orderDate and o.status = :status")
    long sumBookedQuantity(
            @Param("offeringId") Long offeringId,
            @Param("orderDate") LocalDate orderDate,
            @Param("status") OrderStatus status);
}
