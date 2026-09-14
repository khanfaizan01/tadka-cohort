package com.tadka.controller;

import com.tadka.controller.dto.*;
import com.tadka.domain.restaurants.MenuItem;
import com.tadka.domain.restaurants.Restaurant;
import com.tadka.domain.valueobjects.Money;
import com.tadka.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/restaurants")
public class RestaurantsController {

    private final RestaurantService restaurantService;

    public RestaurantsController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping
    public ResponseEntity<List<Restaurant>> getAll(
            @RequestParam(required = false) String city,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(restaurantService.getAll(city, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Restaurant> getById(@PathVariable UUID id) {
        try {
            return ResponseEntity.ok(restaurantService.getById(id));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Restaurant> create(@Valid @RequestBody CreateRestaurantRequest request) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.getName());
        restaurant.setIsActive(true);
        restaurant.setAvgPrepTimeMinutes(request.getAvgPrepTimeMinutes());
        restaurant.setCreatedAt(java.time.LocalDateTime.now());
        return ResponseEntity.created(null).body(restaurantService.create(restaurant));
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<List<MenuItem>> getMenu(
            @PathVariable UUID id,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Boolean vegOnly) {
        return ResponseEntity.ok(restaurantService.getMenu(id, category, vegOnly));
    }

    @PatchMapping("/{id}/menu/{itemId}/availability")
    public ResponseEntity<Void> updateMenuItemAvailability(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateAvailabilityRequest request) {
        restaurantService.updateMenuItemAvailability(id, itemId, request.getIsAvailable());
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Void> update(@PathVariable UUID id, @Valid @RequestBody UpdateRestaurantRequest request) {
        Restaurant update = new Restaurant();
        update.setName(request.getName());
        update.setAvgPrepTimeMinutes(request.getAvgPrepTimeMinutes());
        update.setIsActive(request.getIsActive());
        restaurantService.update(id, update);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/menu")
    public ResponseEntity<MenuItem> addMenuItem(
            @PathVariable UUID id,
            @Valid @RequestBody CreateMenuItemRequest request) {
        MenuItem item = new MenuItem();
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setCategory(request.getCategory());
        item.setIsAvailable(true);
        item.setIsVeg(request.getIsVeg());
        item.setPrice(new Money(request.getPrice().getAmount(), request.getPrice().getCurrency()));
        return ResponseEntity.created(null).body(restaurantService.addMenuItem(id, item));
    }

    @PatchMapping("/{id}/menu/{itemId}")
    public ResponseEntity<Void> updateMenuItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateMenuItemRequest request) {
        MenuItem update = new MenuItem();
        update.setName(request.getName());
        update.setDescription(request.getDescription());
        update.setCategory(request.getCategory());
        update.setIsVeg(request.getIsVeg());
        update.setIsAvailable(request.getIsAvailable());
        if (request.getPrice() != null) {
            update.setPrice(new Money(request.getPrice().getAmount(), request.getPrice().getCurrency()));
        }
        restaurantService.updateMenuItem(id, itemId, update);
        return ResponseEntity.ok().build();
    }
}
