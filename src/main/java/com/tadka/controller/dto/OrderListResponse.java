package com.tadka.controller.dto;

import java.util.List;

public class OrderListResponse {
    private List<OrderResponse> content;
    private int totalElements;
    private int page;
    private int size;

    public OrderListResponse(List<OrderResponse> content, int page, int size, int totalElements) {
        this.content = content; this.page = page; this.size = size; this.totalElements = totalElements;
    }

    public List<OrderResponse> getContent() { return content; }
    public void setContent(List<OrderResponse> content) { this.content = content; }
    public int getTotalElements() { return totalElements; }
    public void setTotalElements(int totalElements) { this.totalElements = totalElements; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
}
