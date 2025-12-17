package vn.id.tyk.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    // Khi vào localhost:8080/admin -> Trả về giao diện admin.html
    @GetMapping
    public String adminDashboard() {
        return "admin"; 
    }
}