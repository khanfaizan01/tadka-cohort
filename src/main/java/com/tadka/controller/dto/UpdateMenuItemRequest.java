package com.tadka.controller.dto;

public class UpdateMenuItemRequest {

    private String name;
    private String description;
    private MoneyRequest price;
    private String category;
    private Boolean isVeg;
    private Boolean isAvailable;

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
    public Boolean getIsAvailable() { return isAvailable; }
    public void setIsAvailable(Boolean isAvailable) { this.isAvailable = isAvailable; }
}
