package vn.id.tyk.webapp.controller.api;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import vn.id.tyk.webapp.dto.LoginRequest;
import vn.id.tyk.webapp.dto.RegisterRequest;
import vn.id.tyk.webapp.dto.UserResponse;
import vn.id.tyk.webapp.entity.User;
import vn.id.tyk.webapp.service.AuthService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthService authService;

    // Công cụ quản lý Session của Spring Security
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    // 1. Đăng ký (Gọi Service để tạo user)
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request) {
        try {
            User newUser = authService.register(request);
            return ResponseEntity.ok("Registration successful! Welcome " + newUser.getUsername());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // 2. Đăng nhập (Vẫn giữ ở Controller vì liên quan trực tiếp đến HTTP Session)
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest, 
                                       HttpServletRequest request, 
                                       HttpServletResponse response) {
        try {
            // Xác thực username/password
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            // Tạo Security Context và lưu vào Session
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            securityContextRepository.saveContext(context, request, response);

            // Lấy thông tin user từ Service để trả về (Dùng DTO UserResponse cho sạch)
            User user = authService.getUserByUsername(loginRequest.getUsername());
            
            return ResponseEntity.ok(UserResponse.fromEntity(user));

        } catch (Exception e) {
            return ResponseEntity.status(401).body("Invalid username or password!");
        }
    }

    // 3. Lấy profile (Dùng cho FE cập nhật số dư coin)
    @GetMapping("/profile/{username}")
    public ResponseEntity<?> getUserProfile(@PathVariable String username) {
        try {
            User user = authService.getUserByUsername(username);
            return ResponseEntity.ok(UserResponse.fromEntity(user));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    // 4. Logout (Xử lý bởi Spring Security config, nhưng có thể thêm API dummy nếu cần)
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        return ResponseEntity.ok("Logout successful");
    }
}