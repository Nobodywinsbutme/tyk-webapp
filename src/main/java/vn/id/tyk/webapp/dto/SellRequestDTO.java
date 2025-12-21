package vn.id.tyk.webapp.dto;

import lombok.Data;

@Data
public class SellRequestDTO {
    private Long itemId; // ID của vật phẩm trong kho (InventoryItem)
    private Long price;  // Giá người dùng muốn bán
    private int quantity;
}