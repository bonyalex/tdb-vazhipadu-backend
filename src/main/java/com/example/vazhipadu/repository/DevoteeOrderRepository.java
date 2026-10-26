package com.example.vazhipadu.repository;

import com.example.vazhipadu.entity.DevoteeOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DevoteeOrderRepository
        extends JpaRepository<DevoteeOrder, Long>, JpaSpecificationExecutor<DevoteeOrder> {}
