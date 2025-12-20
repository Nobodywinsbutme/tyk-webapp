package vn.id.tyk.webapp.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.id.tyk.webapp.service.InventoryService;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryRestController {

    private final InventoryService inventoryService;

    @GetMapping("/{userId}")
    public ResponseEntity<?> getUserInventory(
            @PathVariable Long userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return ResponseEntity.ok(inventoryService.getMyInventory(userId, type, search, page, size));
    }
}