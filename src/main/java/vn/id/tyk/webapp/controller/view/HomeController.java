package vn.id.tyk.webapp.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.id.tyk.webapp.repository.MarketListingRepository;

@Controller
public class HomeController {

    private final MarketListingRepository marketRepository;

    public HomeController(MarketListingRepository marketRepository) {
        this.marketRepository = marketRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("appName", "TYK Game Marketplace");
        return "home";
    }

    @GetMapping("/marketplace")
    public String marketplace(Model model) {
        model.addAttribute("newItems", marketRepository.findTop10ByStatusOrderByListedAtDesc("ACTIVE"));  
        return "marketplace"; // Trả về file marketplace.html
    }

    @GetMapping("/news")
    public String news() {
        return "news";
    }

    @GetMapping("/guide")
    public String guide() {
        return "guide";
    }

    @GetMapping("/community")
    public String community() {
        return "community";
    }

    @GetMapping("/marketplace/market")
    public String showMarketBrowse(Model model) {
        model.addAttribute("newItems", marketRepository.findTop10ByStatusOrderByListedAtDesc("ACTIVE"));
        return "market";
    }

    @GetMapping("/bid")
    public String showBidBrowse() {
        return "bid";
    }
}