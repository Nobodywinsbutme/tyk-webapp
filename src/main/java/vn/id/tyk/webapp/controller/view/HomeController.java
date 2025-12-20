package vn.id.tyk.webapp.controller.view;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("appName", "TYK Game Marketplace");
        return "home";
    }

    @GetMapping("/marketplace")
    public String marketplace() {
        return "marketplace";
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
        return "market";
    }
}