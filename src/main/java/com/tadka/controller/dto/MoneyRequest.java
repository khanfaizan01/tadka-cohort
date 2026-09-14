package com.tadka.controller.dto;

import jakarta.validation.constraints.*;

public class MoneyRequest {

    @NotNull
    private java.math.BigDecimal amount;

    @NotBlank
    private String currency = "INR";

    public java.math.BigDecimal getAmount() { return amount; }
    public void setAmount(java.math.BigDecimal amount) { this.amount = amount; }
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}
