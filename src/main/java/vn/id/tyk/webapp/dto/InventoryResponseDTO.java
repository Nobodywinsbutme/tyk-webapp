package vn.id.tyk.webapp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryResponseDTO {
    private Long id;              
    private String name;          
    private String imageUrl;      
    private String type;         
    private String rarity;        
    private int quantity;        
    private String description;   
    private boolean isTradable;   
    private boolean isBookmarked;
}