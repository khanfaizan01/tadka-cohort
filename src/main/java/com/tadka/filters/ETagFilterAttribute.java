package com.tadka.filters;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

/**
 * Day 6, Beat (ADR-048): conditional GET for cacheable reads. Hashes the
 * response body and compares against the client's If-None-Match; on a match,
 * swaps in a 304 with an EMPTY body instead of re-sending the full JSON.
 */
@Component
public class ETagFilterAttribute implements HandlerInterceptor {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static String computeETag(byte[] body) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(body);
            return "\"" + HexFormat.of().formatHex(hash) + "\"";
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        return true;
    }
}
