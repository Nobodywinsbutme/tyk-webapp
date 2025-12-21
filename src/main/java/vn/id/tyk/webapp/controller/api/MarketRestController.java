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
}