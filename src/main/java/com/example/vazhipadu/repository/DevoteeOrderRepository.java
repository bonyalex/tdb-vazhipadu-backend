package com.example.vazhipadu.repository;

import com.example.vazhipadu.dto.response.OfferingCollectionResponse;
import com.example.vazhipadu.entity.DevoteeOrder;
import com.example.vazhipadu.entity.OrderStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
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

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from DevoteeOrder o where o.id = :id")
    Optional<DevoteeOrder> findByIdForUpdate(@Param("id") Long id);

    @Query("select new com.example.vazhipadu.dto.response.OfferingCollectionResponse("
            + "l.offering.id, l.offering.name, sum(l.quantity), sum(l.lineTotal))"
            + " from DevoteeOrder o join o.lines l"
            + " where o.temple.id = :templeId and o.orderDate = :orderDate and o.status = :status"
            + " group by l.offering.id, l.offering.name order by l.offering.id")
    List<OfferingCollectionResponse> collectionByOffering(
            @Param("templeId") Long templeId,
            @Param("orderDate") LocalDate orderDate,
            @Param("status") OrderStatus status);
}
