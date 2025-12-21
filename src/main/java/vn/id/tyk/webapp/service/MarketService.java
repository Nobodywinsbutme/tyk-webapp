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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MarketService {
    private final MarketListingRepository marketRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository; // Giả sử bạn đã có cái này


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
                        .quantity(m.getQuantity()) // Thêm hiển thị số lượng
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void sellItem(Long userId, SellRequestDTO request) {
        // Tìm vật phẩm trong kho
        InventoryItem item = inventoryRepository.findById(request.getItemId())
            .orElseThrow(() -> new RuntimeException("Item not found"));

        if (!item.getUser().getId().equals(userId)) {
            throw new RuntimeException("You don't own this item");
        }

        if (item.getQuantity() < request.getQuantity()) {
            throw new RuntimeException("Not enough items in inventory!");
        }

        // Tạo bản ghi bán hàng
        MarketListing listing = new MarketListing();
        listing.setSeller(item.getUser());
        
        // --- SỬA QUAN TRỌNG: Lưu Definition thay vì Item ---
        listing.setItemDefinition(item.getItemDefinition()); 
        
        listing.setPrice(request.getPrice());
        listing.setQuantity(request.getQuantity());
        listing.setStatus("ACTIVE");
        listing.setListedAt(LocalDateTime.now());

        marketRepository.save(listing);

        // Trừ kho hàng
        int newQuantity = item.getQuantity() - request.getQuantity();

        if (newQuantity > 0) {
            item.setQuantity(newQuantity);
            inventoryRepository.save(item);
        } else {
            // --- SỬA: Xóa thoải mái mà không sợ lỗi TransientObjectException ---
            inventoryRepository.delete(item);
        }
    }

    @Transactional
    public void buyItem(Long buyerId, Long listingId) {
        // Tìm đơn hàng
        MarketListing listing = marketRepository.findById(listingId)
            .orElseThrow(() -> new RuntimeException("Listing not found"));

        if (!"ACTIVE".equals(listing.getStatus())) {
            throw new RuntimeException("Item is no longer available");
        }
        
        // Không cho phép tự mua hàng của mình
        if (listing.getSeller().getId().equals(buyerId)) {
             throw new RuntimeException("Cannot buy your own item");
        }

        User buyer = userRepository.findById(buyerId).orElseThrow();
        User seller = listing.getSeller();

        // Kiểm tra tiền
        if (buyer.getCoinBalance() < listing.getPrice()) {
            throw new RuntimeException("Not enough coins!");
        }

        // Giao dịch tiền
        buyer.setCoinBalance(buyer.getCoinBalance() - listing.getPrice());
        seller.setCoinBalance(seller.getCoinBalance() + listing.getPrice());

        // --- SỬA: CHUYỂN VẬT PHẨM (Logic: Tái tạo vật phẩm cho người mua) ---
        
        // 1. Kiểm tra xem người mua đã có vật phẩm loại này trong túi chưa?
        Optional<InventoryItem> existingItemOpt = inventoryRepository.findByUserAndItemDefinition(buyer, listing.getItemDefinition());

        if (existingItemOpt.isPresent()) {
            // Nếu có rồi -> Cộng dồn số lượng
            InventoryItem existingItem = existingItemOpt.get();
            existingItem.setQuantity(existingItem.getQuantity() + listing.getQuantity());
            inventoryRepository.save(existingItem);
        } else {
            // Nếu chưa có -> Tạo mới hoàn toàn
            InventoryItem newItem = new InventoryItem();
            newItem.setUser(buyer);
            newItem.setItemDefinition(listing.getItemDefinition()); // Lấy thông tin từ Listing
            newItem.setQuantity(listing.getQuantity());
            inventoryRepository.save(newItem);
        }

        // Cập nhật trạng thái đơn hàng -> SOLD
        listing.setStatus("SOLD");
        marketRepository.save(listing);
        
        userRepository.save(buyer);
        userRepository.save(seller);
    }
}