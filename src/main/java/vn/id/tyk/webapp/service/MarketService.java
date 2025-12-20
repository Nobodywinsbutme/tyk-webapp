package vn.id.tyk.webapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.id.tyk.webapp.dto.MarketItemDTO;
import vn.id.tyk.webapp.entity.ItemType;
import vn.id.tyk.webapp.repository.MarketListingRepository;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketService {
    private final MarketListingRepository marketRepository;

    public List<MarketItemDTO> getAllMarketItems(String type, Double minPrice, Double maxPrice, String search) {
        ItemType itemType = null;
        if (type != null && !type.isEmpty()) {
            try {
                itemType = ItemType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                itemType = null;
            }
        }

        return marketRepository.filterMarket(itemType, minPrice, maxPrice, search)
                .stream()
                .map(m -> MarketItemDTO.builder()
                        .id(m.getId())
                        
                        // SỬA: Gọi qua .getItem()
                        .name(m.getItem().getItemDefinition().getName()) 
                        .imageUrl(m.getItem().getItemDefinition().getImageUrl())
                        
                        // SỬA: Chuyển đổi Long sang Double an toàn
                        .price(m.getPrice() != null ? m.getPrice().doubleValue() : 0.0)
                        
                        .sellerName(m.getSeller().getUsername())
                        
                        // SỬA: Gọi qua .getItem()
                        .type(m.getItem().getItemDefinition().getType().toString())
                        .build())
                .collect(Collectors.toList());
    }
}