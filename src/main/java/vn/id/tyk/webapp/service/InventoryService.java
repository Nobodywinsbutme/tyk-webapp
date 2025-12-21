package vn.id.tyk.webapp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.id.tyk.webapp.dto.InventoryResponseDTO;
import vn.id.tyk.webapp.entity.*;
import vn.id.tyk.webapp.repository.InventoryRepository;
import vn.id.tyk.webapp.repository.MarketListingRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final MarketListingRepository marketListingRepository;

    // --- 1. LẤY DANH SÁCH (VIEW) ---
    public Page<InventoryResponseDTO> getMyInventory(Long userId, String tab, String typeStr, String rarityStr, String keyword, int page, int size) {
        
        // Xử lý Enum Type
        ItemType type = null;
        if (typeStr != null && !typeStr.isEmpty() && !typeStr.equalsIgnoreCase("ALL")) {
            try { type = ItemType.valueOf(typeStr.toUpperCase()); } catch (IllegalArgumentException e) { type = null; }
        }
        
        // Xử lý Enum Rarity
        ItemRarity rarity = null;
        if (rarityStr != null && !rarityStr.isEmpty() && !rarityStr.equalsIgnoreCase("ALL")) {
            try { rarity = ItemRarity.valueOf(rarityStr.toUpperCase()); } catch (IllegalArgumentException e) { rarity = null; }
        }

        Pageable pageable = PageRequest.of(page - 1, size, Sort.by("id").descending());

        // A. Tab Đang Bán (Lấy từ MarketListing)
        if ("on_sale".equalsIgnoreCase(tab)) {
            Page<MarketListing> marketPage = marketListingRepository.findBySellerIdAndStatus(userId, "ACTIVE", pageable);
            
            return marketPage.map(listing -> InventoryResponseDTO.builder()
                    .id(listing.getId()) // ID này dùng để Hủy/Sửa
                    .name(listing.getItemDefinition().getName())
                    .imageUrl(listing.getItemDefinition().getImageUrl())
                    .type(listing.getItemDefinition().getType().toString())
                    .rarity(listing.getItemDefinition().getRarity().toString())
                    .quantity(listing.getQuantity())
                    .description(listing.getItemDefinition().getDescription())
                    .price(listing.getPrice()) 
                    .listedAt(listing.getListedAt().toString()) 
                    .isTradable(true)
                    .isBookmarked(false)
                    .build());
        }

        // B. Tab Trong Kho (Lấy từ InventoryItem)
        Page<InventoryItem> rawPage = inventoryRepository.findByUserAndFilter(userId, type, rarity, keyword, pageable);

        return rawPage.map(item -> InventoryResponseDTO.builder()
                .id(item.getId()) // ID này dùng để Bán
                .name(item.getItemDefinition().getName())
                .imageUrl(item.getItemDefinition().getImageUrl())
                .type(item.getItemDefinition().getType().toString())
                .rarity(item.getItemDefinition().getRarity() != null ? item.getItemDefinition().getRarity().toString() : "COMMON")
                .quantity(item.getQuantity())
                .description(item.getItemDefinition().getDescription())
                .isTradable(true) 
                .isBookmarked(false) 
                .build());
    }

    // --- 2. HÀM CỘNG ĐỒ VÀO KHO (LOGIC DÙNG CHUNG) ---
    // Hàm này sẽ được gọi từ MarketService khi Mua thành công hoặc Hủy bán
    @Transactional
    public void addItemToInventory(User user, ItemDefinition itemDefinition, int quantity) {
        // Kiểm tra xem user đã có vật phẩm này chưa
        Optional<InventoryItem> existingItemOpt = inventoryRepository.findByUserAndItemDefinition(user, itemDefinition);

        if (existingItemOpt.isPresent()) {
            // Có rồi -> Cộng dồn số lượng
            InventoryItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            inventoryRepository.save(existingItem);
        } else {
            // Chưa có -> Tạo dòng mới
            InventoryItem newItem = new InventoryItem();
            newItem.setUser(user);
            newItem.setItemDefinition(itemDefinition);
            newItem.setQuantity(quantity);
            inventoryRepository.save(newItem);
        }
    }
}