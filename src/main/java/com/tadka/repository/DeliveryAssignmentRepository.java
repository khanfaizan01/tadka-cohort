package com.tadka.repository;

import com.tadka.domain.delivery.DeliveryAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface DeliveryAssignmentRepository extends JpaRepository<DeliveryAssignment, UUID> {
}
