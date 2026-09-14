package com.tadka.domain.orders;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tadka.domain.valueobjects.Money;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "order_items", schema = "ordering")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    @JsonIgnore
    private Order order;

    @Column(name = "menu_item_id", nullable = false)
    private UUID menuItemId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer quantity;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount", column = @Column(name = "unit_price_amount")),
        @AttributeOverride(name = "currency", column = @Column(name = "unit_price_currency"))
    })
    private Money unitPrice;

    @Column(name = "special_instructions", nullable = true)
    private String specialInstructions;
}
