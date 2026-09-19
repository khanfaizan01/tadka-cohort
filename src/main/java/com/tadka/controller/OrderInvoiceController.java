package com.tadka.controller;

import com.tadka.controller.dto.InvoiceResponse;
import com.tadka.controller.dto.SignedInvoiceUrlResponse;

import com.tadka.domain.orders.Order;
import com.tadka.infrastructure.security.UrlSigner;
import com.tadka.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Day 6, Beat (CDN emulation - signed URLs, ADR-050): a time-limited link to a resource.
 * No PDF generation - the receipt is JSON; the point being taught is the signing mechanic.
 */
@RestController
@RequestMapping("/api/v1/orders/{id}/invoice")
public class OrderInvoiceController {

    private final OrderRepository orders;
    private final UrlSigner signer;
    private static final Duration DEFAULT_VALIDITY = Duration.ofMinutes(5);

    public OrderInvoiceController(OrderRepository orders, UrlSigner signer) {
        this.orders = orders;
        this.signer = signer;
    }

    @PostMapping("/sign")
    public ResponseEntity<SignedInvoiceUrlResponse> sign(@PathVariable UUID id) {
        Order order = orders.findByIdWithItems(id);
        if (order == null) return ResponseEntity.notFound().build();

        UrlSigner.SignedUrl signed = signer.sign(id.toString(), DEFAULT_VALIDITY);
        String url = "/api/v1/orders/" + id + "/invoice?sig=" + signed.signature() + "&exp=" + signed.expiresAtUnixSeconds();
        return ResponseEntity.ok(new SignedInvoiceUrlResponse(url, LocalDateTime.now().plus(DEFAULT_VALIDITY)));
    }

    @GetMapping
    public ResponseEntity<InvoiceResponse> get(@PathVariable UUID id,
                                                @RequestParam String sig,
                                                @RequestParam long exp) {
        if (sig == null || sig.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (!signer.verify(id.toString(), exp, sig)) {
            return ResponseEntity.status(403).build();
        }

        Order order = orders.findByIdWithItems(id);
        if (order == null) return ResponseEntity.notFound().build();

        return ResponseEntity.ok(new InvoiceResponse(
            order.getId(), order.getCustomerId(),
            order.getTotalAmount().getAmount(),
            order.getTotalAmount().getCurrency(),
            order.getStatus().name(),
            order.getCreatedAt()));
    }
}
