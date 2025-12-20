package vn.id.tyk.webapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
public class MarketListing {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người bán là ai?
    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    // Bán cái gì? (Liên kết tới vật phẩm trong kho hoặc Design)
    @OneToOne
    @JoinColumn(name = "inventory_item_id")
    private InventoryItem item; 

    // Giá bán bao nhiêu?
    private Long price;

    // Thời gian đăng bán
    private LocalDateTime listedAt = LocalDateTime.now();

    // Trạng thái: ACTIVE (Đang bán), SOLD (Đã bán), CANCELLED (Đã gỡ)
    private String status; 
}