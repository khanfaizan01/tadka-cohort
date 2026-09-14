package com.tadka.repository;

import com.tadka.domain.orders.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.customerId = :customerId ORDER BY o.createdAt DESC")
    List<Order> findByCustomerId(@Param("customerId") UUID customerId);

    @Query("SELECT o FROM Order o JOIN FETCH o.items WHERE o.id = :id")
    Order findByIdWithItems(@Param("id") UUID id);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.customerId = :customerId")
    long countByCustomerId(@Param("customerId") UUID customerId);

    @Query("SELECT o FROM Order o WHERE o.customerId = :customerId ORDER BY o.createdAt DESC")
    List<Order> findByCustomerIdPaginated(@Param("customerId") UUID customerId,
                                           @Param("page") int page,
                                           @Param("size") int size);
}
