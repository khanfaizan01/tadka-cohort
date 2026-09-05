package com.tadka.domain.delivery;

import com.tadka.domain.valueobjects.GeoLocation;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "delivery_agents", schema = "delivery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryAgent {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private AgentStatus status;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "latitude", column = @Column(name = "current_location_latitude")),
        @AttributeOverride(name = "longitude", column = @Column(name = "current_location_longitude"))
    })
    private GeoLocation currentLocation;
}
