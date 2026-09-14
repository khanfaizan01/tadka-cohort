package com.tadka.controller;

import com.tadka.TadkaApplication;
import com.tadka.domain.orders.Order;
import com.tadka.domain.orders.OrderStatus;
import com.tadka.domain.valueobjects.Money;
import com.tadka.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TadkaApplication.class)
@Testcontainers
@Sql(scripts = {
    "/setup-test-data.sql"
})
class OrderPlacementIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("tadka")
            .withUsername("tadka")
            .withPassword("tadka_local");

    @DynamicPropertySource
    static void overrideDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void orderCanBeCreatedWithStatusCreated() {
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setCustomerId(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"));
        order.setRestaurantId(UUID.fromString("6ba7b810-9dad-11d1-80b4-00c04fd430c8"));
        order.setStatus(OrderStatus.CREATED);
        order.setTotalAmount(new Money(new BigDecimal("560.00"), "INR"));
        order.setCreatedAt(java.time.LocalDateTime.now());

        orderRepository.save(order);

        Order found = orderRepository.findById(order.getId()).orElse(null);
        assertThat(found).isNotNull();
        assertThat(found.getStatus()).isEqualTo(OrderStatus.CREATED);
    }
}
