package vn.id.tyk.webapp.controller.api;

import lombok.RequiredArgsConstructor;
import vn.id.tyk.webapp.dto.MarketItemDTO;
import vn.id.tyk.webapp.service.MarketService;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.id.tyk.webapp.dto.SellRequestDTO;

@RestController
@RequestMapping("/api/market")
@RequiredArgsConstructor
public class MarketRestController {
    private final MarketService marketService; // Gọi qua Service

    @GetMapping("/listings")
    public List<MarketItemDTO> getListings(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) String search) {
        
        return marketService.getAllMarketItems(type, minPrice, maxPrice, search);
    }

    @PostMapping("/sell/{userId}")
    public ResponseEntity<?> sellItem(@PathVariable Long userId, @RequestBody SellRequestDTO request) {
        try {
            marketService.sellItem(userId, request);
            return ResponseEntity.ok("Item listed successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    
    @PostMapping("/buy/{userId}/{listingId}")
    public ResponseEntity<?> buyItem(@PathVariable Long userId, @PathVariable Long listingId) {
        try {
            marketService.buyItem(userId, listingId);
            return ResponseEntity.ok("Purchase successful");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 4. Hủy bán (Cancel) -> Cho nút Thùng rác
    @DeleteMapping("/{listingId}")
    public ResponseEntity<?> cancelListing(@PathVariable Long listingId) {
        try {
            marketService.cancelListing(listingId);
            return ResponseEntity.ok("Listing cancelled");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 5. Sửa giá (Update Price) -> Cho nút Bút chì
    @PutMapping("/{listingId}")
    public ResponseEntity<?> updatePrice(@PathVariable Long listingId, @RequestParam Long price) {
        try {
            marketService.updatePrice(listingId, price);
            return ResponseEntity.ok("Price updated");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}