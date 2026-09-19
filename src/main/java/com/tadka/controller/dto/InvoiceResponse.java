package com.tadka.controller.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record InvoiceResponse(UUID orderId, UUID customerId, BigDecimal amount, String currency, String status, LocalDateTime createdAt) {}
