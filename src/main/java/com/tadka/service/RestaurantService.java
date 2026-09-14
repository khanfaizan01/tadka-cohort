package com.tadka.service;

import com.tadka.domain.restaurants.MenuItem;
import com.tadka.domain.restaurants.Restaurant;
import com.tadka.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    @Transactional(readOnly = true)
    public List<Restaurant> getAll(String city, int page, int size) {
        return restaurantRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Restaurant getById(UUID id) {
        return restaurantRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));
    }

    @Transactional(readOnly = true)
    public Restaurant getByIdWithMenu(UUID id) {
        return restaurantRepository.findByIdWithMenu(id)
            .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));
    }

    @Transactional
    public Restaurant create(Restaurant restaurant) {
        restaurant.setId(UUID.randomUUID());
        restaurant.setCreatedAt(java.time.LocalDateTime.now());
        return restaurantRepository.save(restaurant);
    }

    @Transactional
    public void update(UUID id, Restaurant update) {
        Restaurant existing = restaurantRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Restaurant not found"));
        if (update.getName() != null) existing.setName(update.getName());
        if (update.getAvgPrepTimeMinutes() != null) existing.setAvgPrepTimeMinutes(update.getAvgPrepTimeMinutes());
        if (update.getIsActive() != null) existing.setIsActive(update.getIsActive());
        restaurantRepository.save(existing);
    }

    @Transactional(readOnly = true)
    public List<MenuItem> getMenu(UUID restaurantId, String category, Boolean vegOnly) {
        Restaurant restaurant = getByIdWithMenu(restaurantId);
        List<MenuItem> items = restaurant.getMenu();
        if (category != null) {
            items = items.stream().filter(i -> i.getCategory().equalsIgnoreCase(category)).toList();
        }
        if (vegOnly != null && vegOnly) {
            items = items.stream().filter(MenuItem::getIsVeg).toList();
        }
        return items;
    }

    @Transactional
    public MenuItem addMenuItem(UUID restaurantId, MenuItem item) {
        Restaurant restaurant = getByIdWithMenu(restaurantId);
        item.setId(UUID.randomUUID());
        item.setRestaurant(restaurant);
        restaurant.getMenu().add(item);
        return restaurantRepository.save(restaurant).getMenu().get(restaurant.getMenu().size() - 1);
    }

    @Transactional
    public void updateMenuItemAvailability(UUID restaurantId, UUID itemId, boolean available) {
        Restaurant restaurant = getByIdWithMenu(restaurantId);
        MenuItem item = restaurant.getMenu().stream()
            .filter(m -> m.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("MenuItem not found"));
        item.setIsAvailable(available);
        restaurantRepository.save(restaurant);
    }

    @Transactional
    public void updateMenuItem(UUID restaurantId, UUID itemId, MenuItem update) {
        Restaurant restaurant = getByIdWithMenu(restaurantId);
        MenuItem existing = restaurant.getMenu().stream()
            .filter(m -> m.getId().equals(itemId))
            .findFirst()
            .orElseThrow(() -> new EntityNotFoundException("MenuItem not found"));
        if (update.getName() != null) existing.setName(update.getName());
        if (update.getDescription() != null) existing.setDescription(update.getDescription());
        if (update.getPrice() != null) existing.setPrice(update.getPrice());
        if (update.getCategory() != null) existing.setCategory(update.getCategory());
        if (update.getIsVeg() != null) existing.setIsVeg(update.getIsVeg());
        if (update.getIsAvailable() != null) existing.setIsAvailable(update.getIsAvailable());
        restaurantRepository.save(restaurant);
    }
}
