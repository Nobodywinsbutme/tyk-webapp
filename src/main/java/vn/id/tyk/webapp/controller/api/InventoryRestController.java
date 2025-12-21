package vn.id.tyk.webapp.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.id.tyk.webapp.service.InventoryService;
import vn.id.tyk.webapp.service.MarketService;
import vn.id.tyk.webapp.dto.SellRequestDTO;
import vn.id.tyk.webapp.dto.InventoryResponseDTO;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryRestController {

    private final InventoryService inventoryService;

    private final MarketService marketService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserInventory(
            @PathVariable Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "all") String tab,
            @RequestParam(required = false) String rarity,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return ResponseEntity.ok(inventoryService.getMyInventory(userId, tab, type, rarity, search, page, size));
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