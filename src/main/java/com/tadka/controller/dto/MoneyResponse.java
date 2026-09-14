package com.tadka.controller.dto;

import java.math.BigDecimal;

public class MoneyResponse {
    private BigDecimal amount;
    private String currency;

    public MoneyResponse(BigDecimal amount, String currency) {
        this.amount = amount; this.currency = currency;
    }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
