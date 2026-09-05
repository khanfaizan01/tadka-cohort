package com.tadka.domain.restaurants;

import com.tadka.domain.valueobjects.Address;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "restaurants", schema = "restaurant")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Restaurant {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "line1", column = @Column(name = "address_line1")),
        @AttributeOverride(name = "line2", column = @Column(name = "address_line2")),
        @AttributeOverride(name = "city", column = @Column(name = "address_city")),
        @AttributeOverride(name = "pincode", column = @Column(name = "address_pincode")),
        @AttributeOverride(name = "latitude", column = @Column(name = "address_latitude")),
        @AttributeOverride(name = "longitude", column = @Column(name = "address_longitude"))
    })
    private Address address;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MenuItem> menu;

    @Column(name = "avg_prep_time_minutes")
    private Integer avgPrepTimeMinutes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
