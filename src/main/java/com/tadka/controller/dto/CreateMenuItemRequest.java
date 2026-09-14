package com.tadka.controller.dto;

import jakarta.validation.constraints.*;

public class CreateMenuItemRequest {

    @NotBlank
    private String name;

    private String description;

    @NotNull
    private MoneyRequest price;

    @NotBlank
    private String category;

    @NotNull
    private Boolean isVeg;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public MoneyRequest getPrice() { return price; }
    public void setPrice(MoneyRequest price) { this.price = price; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public Boolean getIsVeg() { return isVeg; }
    public void setIsVeg(Boolean isVeg) { this.isVeg = isVeg; }
}
