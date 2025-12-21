package vn.id.tyk.webapp.service;

import org.springframework.transaction.annotation.Transactional;
import vn.id.tyk.webapp.dto.SellRequestDTO;
import vn.id.tyk.webapp.entity.*;
import vn.id.tyk.webapp.repository.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.id.tyk.webapp.dto.MarketItemDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MarketService {
    
    private final MarketListingRepository marketRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    
    // Inject InventoryService để tái sử dụng hàm addItemToInventory
    private final InventoryService inventoryService; 

    // --- 1. LẤY DANH SÁCH CHỢ ---
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
                        .name(m.getItemDefinition().getName())
                        .imageUrl(m.getItemDefinition().getImageUrl())
                        .price(m.getPrice() != null ? m.getPrice().doubleValue() : 0.0)
                        .sellerName(m.getSeller().getUsername())
                        .type(m.getItemDefinition().getType().toString())
                        .quantity(m.getQuantity())
                        .build())
                .collect(Collectors.toList());
    }

    // --- 2. ĐĂNG BÁN (SELL) ---
    @Transactional
    public void sellItem(Long userId, SellRequestDTO request) {
        InventoryItem item = inventoryRepository.findById(request.getItemId())
            .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!item.getUser().getId().equals(userId)) {
            throw new RuntimeException("You don't own this item");
        }

        if (item.getQuantity() < request.getQuantity()) {
            throw new RuntimeException("Not enough items!");
        }

        // Tạo Listing
        MarketListing listing = new MarketListing();
        listing.setSeller(item.getUser());
        listing.setItemDefinition(item.getItemDefinition());
        listing.setPrice(request.getPrice());
        listing.setQuantity(request.getQuantity());
        listing.setStatus("ACTIVE");
        listing.setListedAt(LocalDateTime.now());

        marketRepository.save(listing);

        // Trừ kho
        int newQuantity = item.getQuantity() - request.getQuantity();
        if (newQuantity > 0) {
            item.setQuantity(newQuantity);
            inventoryRepository.save(item);
        } else {
            inventoryRepository.delete(item);
        }
    }

    // --- 3. MUA HÀNG (BUY) ---
    @Transactional
    public void buyItem(Long buyerId, Long listingId) {
        MarketListing listing = marketRepository.findById(listingId)
            .orElseThrow(() -> new RuntimeException("Listing not found"));

        if (!"ACTIVE".equals(listing.getStatus())) {
            throw new RuntimeException("Item unavailable");
        }
        
        if (listing.getSeller().getId().equals(buyerId)) {
             throw new RuntimeException("Cannot buy your own item");
        }

        User buyer = userRepository.findById(buyerId).orElseThrow();
        User seller = listing.getSeller();

        if (buyer.getCoinBalance() < listing.getPrice()) {
            throw new RuntimeException("Not enough coins!");
        }

        // Giao dịch tiền
        buyer.setCoinBalance(buyer.getCoinBalance() - listing.getPrice());
        seller.setCoinBalance(seller.getCoinBalance() + listing.getPrice());

        // --- GỌI INVENTORY SERVICE ĐỂ CHUYỂN ĐỒ ---
        inventoryService.addItemToInventory(buyer, listing.getItemDefinition(), listing.getQuantity());

        // Kết thúc giao dịch
        listing.setStatus("SOLD");
        marketRepository.save(listing);
        
        userRepository.save(buyer);
        userRepository.save(seller);
    }
    
    // --- 4. HỦY BÁN (CANCEL) ---
    @Transactional
    public void cancelListing(Long listingId) {
        MarketListing listing = marketRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));

        // --- GỌI INVENTORY SERVICE ĐỂ TRẢ ĐỒ VỀ KHO ---
        inventoryService.addItemToInventory(listing.getSeller(), listing.getItemDefinition(), listing.getQuantity());

        // Xóa Listing
        marketRepository.delete(listing);
    }

    // --- 5. SỬA GIÁ (UPDATE PRICE) ---
    @Transactional
    public void updatePrice(Long listingId, Long newPrice) {
        MarketListing listing = marketRepository.findById(listingId)
                .orElseThrow(() -> new RuntimeException("Listing not found"));
        
        listing.setPrice(newPrice);
        marketRepository.save(listing);
    }
}