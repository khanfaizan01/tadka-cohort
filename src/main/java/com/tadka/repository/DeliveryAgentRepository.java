package com.tadka.repository;

import com.tadka.domain.delivery.DeliveryAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DeliveryAgentRepository extends JpaRepository<DeliveryAgent, UUID> {
}
