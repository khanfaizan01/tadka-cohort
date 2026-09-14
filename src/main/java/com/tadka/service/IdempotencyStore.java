package com.tadka.service;

import com.tadka.domain.orders.IdempotencyKey;
import com.tadka.repository.IIdempotencyStore;
import com.tadka.repository.IdempotencyKeyRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class IdempotencyStore implements IIdempotencyStore {

    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public IdempotencyStore(IdempotencyKeyRepository idempotencyKeyRepository) {
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    @Override
    public UUID findOrderId(String key) {
        return idempotencyKeyRepository.findByKey(key)
            .map(IdempotencyKey::getOrderId)
            .orElse(null);
    }

    @Override
    public void record(String key, UUID orderId) {
        IdempotencyKey ik = new IdempotencyKey();
        ik.setKey(key);
        ik.setOrderId(orderId);
        ik.setCreatedAt(LocalDateTime.now());
        idempotencyKeyRepository.save(ik);
    }
}
