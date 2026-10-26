package com.example.vazhipadu.repository;

import com.example.vazhipadu.entity.Offering;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferingRepository extends JpaRepository<Offering, Long> {

    Page<Offering> findByTempleId(Long templeId, Pageable pageable);
}
