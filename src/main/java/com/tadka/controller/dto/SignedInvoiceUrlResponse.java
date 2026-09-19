package com.tadka.controller.dto;

import java.time.LocalDateTime;

public record SignedInvoiceUrlResponse(String url, LocalDateTime expiresAt) {}
