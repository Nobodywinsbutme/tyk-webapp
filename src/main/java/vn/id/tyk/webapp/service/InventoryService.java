package vn.id.tyk.webapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import vn.id.tyk.webapp.dto.InventoryResponseDTO;
import vn.id.tyk.webapp.entity.InventoryItem;
import vn.id.tyk.webapp.entity.ItemType;
import vn.id.tyk.webapp.entity.ItemRarity;
import vn.id.tyk.webapp.repository.InventoryRepository;
import vn.id.tyk.webapp.repository.MarketListingRepository;
import vn.id.tyk.webapp.entity.MarketListing;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    private final MarketListingRepository marketListingRepository;

    public Page<InventoryResponseDTO> getMyInventory(Long userId,String tab, String typeStr, String rarityStr, String keyword, int page, int size) {
        // 1. Xử lý ItemType (Chuyển String sang Enum)
        ItemType type = null;
        if (typeStr != null && !typeStr.isEmpty() && !typeStr.equalsIgnoreCase("ALL")) {
            try {
                type = ItemType.valueOf(typeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                type = null; 
            }
        }
        
        ItemRarity rarity = null;
        if (rarityStr != null && !rarityStr.isEmpty() && !rarityStr.equalsIgnoreCase("ALL")) {
            try {
                rarity = ItemRarity.valueOf(rarityStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                rarity = null;
            }
        }

        // 2. Tạo Pageable (Phân trang, sắp xếp đồ mới nhất lên đầu)
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());

        if ("on_sale".equalsIgnoreCase(tab)) {
            Page<MarketListing> marketPage = marketListingRepository.findBySellerIdAndStatus(userId, "ACTIVE", pageable);
            
            // Map từ MarketListing sang DTO (Lưu ý cách lấy dữ liệu khác một chút)
            return marketPage.map(listing -> InventoryResponseDTO.builder()
                    .id(listing.getId()) // Lấy ID của item gốc
                    .name(listing.getItemDefinition().getName())
                    .imageUrl(listing.getItemDefinition().getImageUrl())
                    .type(listing.getItemDefinition().getType().toString())
                    .rarity(listing.getItemDefinition().getRarity().toString())
                    .quantity(listing.getQuantity()) // Số lượng đang bán
                    .description(listing.getItemDefinition().getDescription())
                    
                    // Hai trường mới thêm vào DTO
                    .price(listing.getPrice()) 
                    .listedAt(listing.getListedAt().toString()) 
                    
                    .isTradable(true)
                    .isBookmarked(false)
                    .build());
        }

        // 3. Gọi Repository
        Page<InventoryItem> rawPage = inventoryRepository.findByUserAndFilter(userId, type, rarity, keyword, pageable);

        // 4. Map Entity sang DTO
        return rawPage.map(item -> InventoryResponseDTO.builder()
                .id(item.getId())
                .name(item.getItemDefinition().getName())
                .imageUrl(item.getItemDefinition().getImageUrl())
                .type(item.getItemDefinition().getType().toString())
                .rarity(item.getItemDefinition().getRarity() != null 
                        ? item.getItemDefinition().getRarity().toString() 
                        : "COMMON")
                .quantity(item.getQuantity())
                .description(item.getItemDefinition().getDescription())
                
                // Logic giả định: Mặc định là cho phép giao dịch
                .isTradable(true) 
                
                // Logic giả định: Chưa có tính năng bookmark trong DB nên để false
                .isBookmarked(false) 
                .build());
    }
}