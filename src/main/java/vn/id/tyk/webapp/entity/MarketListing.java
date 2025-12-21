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

    @ManyToOne
    @JoinColumn(name = "seller_id")
    private User seller;

    @ManyToOne 
    @JoinColumn(name = "item_definition_id", nullable = false)
    private ItemDefinition itemDefinition;

    private Long price;

    private LocalDateTime listedAt = LocalDateTime.now();

    private String status; 

    private Integer quantity;
}