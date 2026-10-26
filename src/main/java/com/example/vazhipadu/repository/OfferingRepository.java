package com.example.vazhipadu.repository;

import com.example.vazhipadu.entity.Offering;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OfferingRepository extends JpaRepository<Offering, Long> {

    Page<Offering> findByTempleId(Long templeId, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Offering o where o.id in :ids order by o.id")
    List<Offering> findAllByIdForUpdate(@Param("ids") List<Long> ids);
}
