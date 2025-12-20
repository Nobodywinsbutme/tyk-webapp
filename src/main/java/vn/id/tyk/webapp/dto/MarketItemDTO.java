package vn.id.tyk.webapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MarketItemDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private Double price;
    private String sellerName;
    private String type;
}