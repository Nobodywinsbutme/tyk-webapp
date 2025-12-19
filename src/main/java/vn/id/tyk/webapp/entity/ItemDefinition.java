package vn.id.tyk.webapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "item_definitions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDefinition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // Tên vật phẩm (VD: Kiếm Rồng)

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl; // Link ảnh icon của vật phẩm

    private ItemType type; 
    
    private ItemRarity rarity;

    private Double basePrice; // Giá tham khảo (để shop bán hoặc gợi ý)
}