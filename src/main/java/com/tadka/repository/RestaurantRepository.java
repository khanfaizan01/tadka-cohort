package com.tadka.repository;

import com.tadka.domain.restaurants.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID> {

    @Query("SELECT r FROM Restaurant r JOIN FETCH r.menu WHERE r.id = :id")
    Optional<Restaurant> findByIdWithMenu(@Param("id") UUID id);
}
