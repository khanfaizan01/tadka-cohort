package com.tadka.infrastructure.security;

import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Day 6, Beat (CDN emulation - signed URLs, ADR-050): HMAC-SHA256 over
 * "{resourceId}|{expiryUnixSeconds}", hex-encoded. Time-limited, tamper-evident,
 * no server-side state. This is the same mechanic real CDNs / S3 presigned URLs use.
 */
@Component
public class UrlSigner {

    private final byte[] key;

    public UrlSigner() {
        this(System.getenv().getOrDefault("Demo:InvoiceSigningKey", "tadka-demo-signing-key-do-not-use-in-prod"));
    }

    public UrlSigner(String secret) {
        this.key = secret.getBytes(StandardCharsets.UTF_8);
    }

    public SignedUrl sign(String resourceId, java.time.Duration validFor) {
        long expiresAt = System.currentTimeMillis() / 1000 + validFor.toSeconds();
        return new SignedUrl(compute(resourceId, expiresAt), expiresAt);
    }

    public boolean verify(String resourceId, long expiresAtUnixSeconds, String signature) {
        if (signature == null || signature.isEmpty()) return false;
        if (System.currentTimeMillis() / 1000 > expiresAtUnixSeconds) return false;

        String expected = compute(resourceId, expiresAtUnixSeconds);
        return constantTimeEquals(expected, signature);
    }

    private String compute(String resourceId, long expiresAtUnixSeconds) {
        try {
            String payload = resourceId + "|" + expiresAtUnixSeconds;
            Mac hmac = Mac.getInstance("HmacSHA256");
            hmac.init(new SecretKeySpec(key, "HmacSHA256"));
            byte[] hash = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        byte[] aBytes = a.getBytes(StandardCharsets.UTF_8);
        byte[] bBytes = b.getBytes(StandardCharsets.UTF_8);
        if (aBytes.length != bBytes.length) return false;
        int result = 0;
        for (int i = 0; i < aBytes.length; i++) {
            result |= aBytes[i] ^ bBytes[i];
        }
        return result == 0;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public record SignedUrl(String signature, long expiresAtUnixSeconds) {}
}
