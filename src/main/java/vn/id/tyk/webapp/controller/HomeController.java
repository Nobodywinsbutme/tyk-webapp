package vn.id.tyk.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // Lưu ý: Dùng @Controller (để trả về HTML), KHÔNG dùng @RestController
public class HomeController {

    @GetMapping("/") // Khi người dùng vào trang chủ (localhost:8080)
    public String home(Model model) {
        // Gửi một dòng chữ xuống giao diện để chào
        model.addAttribute("appName", "TYK Game Marketplace");
        return "home"; // Trả về file tên là "home.html"
    }

    @GetMapping("/marketplace")
    public String marketplace() {
        return "marketplace"; // Trả về marketplace.html
    }

    @GetMapping("/news")
    public String news() {
        return "news"; // Trả về news.html
    }

    @GetMapping("/guide")
    public String guide() {
        return "guide"; // Trả về guide.html
    }

    @GetMapping("/status")
    public String gameStatus() {
        return "gamestatus"; // Trả về gamestatus.html
    }
}