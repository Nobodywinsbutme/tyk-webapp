package vn.id.tyk.webapp.controller.api;

import vn.id.tyk.webapp.dto.NewsDTO;
import vn.id.tyk.webapp.entity.User;
import vn.id.tyk.webapp.service.NewsService;
import vn.id.tyk.webapp.repository.UserRepository;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/news")
public class NewsController {

    @Autowired
    private NewsService newsService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/list")
    public ResponseEntity<?> getAllNews() {
        try {
            return ResponseEntity.ok(newsService.getAllNews());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error loading news: " + e.getMessage());
        }
    }

    // =========================================================
    // HÀM HELPER: Lấy User chuẩn (Hỗ trợ cả Session & RememberMe)
    // =========================================================
    private User getAuthenticatedUser(HttpSession session, Principal principal) {
        User sessionUser = (User) session.getAttribute("tyk_user");
        if (sessionUser != null) return sessionUser;

        if (principal != null) {
            String username = principal.getName();
            return userRepository.findByUsername(username).orElse(null);
        }

        return null; 
    }

    // API: POST /api/news/create
    @PostMapping("/create")
    // --- SỬA LỖI 3: Thêm tham số Principal principal ---
    public ResponseEntity<?> createNews(@RequestBody NewsDTO newsDTO, HttpSession session, Principal principal) {
        
        // --- SỬA LỖI 2: Phải gọi hàm helper thay vì dùng session trực tiếp ---
        User currentUser = getAuthenticatedUser(session, principal); 

        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Your session has expired. Please log in again!");
        }
        
        if (!"ADMIN".equals(currentUser.getRole())) { 
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You do not have permission to post news!");
        }

        try {
            newsService.createNews(newsDTO, currentUser);
            return ResponseEntity.ok("Post news successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("System error: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    // --- SỬA LỖI 3: Thêm tham số Principal principal ---
    public ResponseEntity<?> updateNews(@PathVariable Long id, @RequestBody NewsDTO newsDTO, HttpSession session, Principal principal) {
        
        // --- SỬA LỖI 2: Gọi hàm helper ---
        User currentUser = getAuthenticatedUser(session, principal);

        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login required");
        if (!"ADMIN".equals(currentUser.getRole())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Permission denied");

        try {
            newsService.updateNews(id, newsDTO);
            return ResponseEntity.ok("Updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete/{id}")
    // --- SỬA LỖI 3: Thêm tham số Principal principal ---
    public ResponseEntity<?> deleteNews(@PathVariable Long id, HttpSession session, Principal principal) {
        
        // --- SỬA LỖI 2: Gọi hàm helper ---
        User currentUser = getAuthenticatedUser(session, principal);

        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login required");
        if (!"ADMIN".equals(currentUser.getRole())) return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Permission denied");

        try {
            newsService.deleteNews(id);
            return ResponseEntity.ok("Deleted successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error: " + e.getMessage());
        }
    }
}