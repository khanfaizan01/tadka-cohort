package com.tadka.domain.users;

import com.tadka.domain.valueobjects.Address;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "user_addresses", schema = "identity")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserAddress {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = true)
    private String label;

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

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault;
}
