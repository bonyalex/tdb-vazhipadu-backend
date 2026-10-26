package com.example.vazhipadu.repository;

import com.example.vazhipadu.entity.DevoteeOrder;
import com.example.vazhipadu.entity.OrderStatus;
import java.time.LocalDate;
import org.springframework.data.jpa.domain.Specification;

public final class OrderSpecifications {

    private OrderSpecifications() {}

    public static Specification<DevoteeOrder> forTemple(Long templeId) {
        return (root, query, builder) -> builder.equal(root.get("temple").get("id"), templeId);
    }

    public static Specification<DevoteeOrder> dateFrom(LocalDate from) {
        return (root, query, builder) -> from == null ? null : builder.greaterThanOrEqualTo(root.get("orderDate"), from);
    }

    public static Specification<DevoteeOrder> dateTo(LocalDate to) {
        return (root, query, builder) -> to == null ? null : builder.lessThanOrEqualTo(root.get("orderDate"), to);
    }

    public static Specification<DevoteeOrder> hasStatus(OrderStatus status) {
        return (root, query, builder) -> status == null ? null : builder.equal(root.get("status"), status);
    }

    public static Specification<DevoteeOrder> hasPhone(String phone) {
        return (root, query, builder) -> phone == null ? null : builder.equal(root.get("devoteePhone"), phone);
    }
}
