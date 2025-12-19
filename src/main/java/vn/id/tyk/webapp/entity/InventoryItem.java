package vn.id.tyk.webapp.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "inventory_items")
@Data
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Đồ này của ai?

    @ManyToOne
    @JoinColumn(name = "item_id", nullable = false)
    private ItemDefinition itemDefinition; // Đây là vật phẩm gì?

    private int quantity; // Số lượng đang có (VD: 5 bình máu)
}