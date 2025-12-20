package vn.id.tyk.webapp.controller.api;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.id.tyk.webapp.dto.MarketItemDTO;
import vn.id.tyk.webapp.service.MarketService;
import java.util.List;


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
}