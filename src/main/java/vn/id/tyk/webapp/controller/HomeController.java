package vn.id.tyk.webapp.controller; // Nhớ dòng này phải khớp với thư mục

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {
    @GetMapping("/")
    public String home() {
        return "Web da chay ngon lanh roi nhe!";
    }
}