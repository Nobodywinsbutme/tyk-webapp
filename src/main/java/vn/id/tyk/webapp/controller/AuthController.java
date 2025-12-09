package vn.id.tyk.webapp.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import vn.id.tyk.webapp.dto.LoginRequest;
import vn.id.tyk.webapp.dto.RegisterRequest;
import vn.id.tyk.webapp.entity.User;
import vn.id.tyk.webapp.repository.UserRepository;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth") // Đường dẫn gốc: localhost:8080/api/auth
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Gọi công cụ mã hóa

    @PostMapping("/register") // Đường dẫn con: /api/auth/register
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request, BindingResult result) {
        
        // 1. Check lỗi Validation (Email sai, pass ngắn...)
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldError().getDefaultMessage());
        }
        
        // 2. Kiểm tra trùng username
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username exit!");
        }

        // 2. Tạo User mới
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(request.getPassword()); // Lưu ý: Chưa mã hóa pass (làm sau)
        newUser.setEmail(request.getEmail());
        newUser.setCoinBalance(0L); // Mới tạo thì tiền bằng 0
        newUser.setRole("USER");

        // MÃ HÓA PASSWORD TRƯỚC KHI LƯU
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        // 3. Lưu xuống Database
        userRepository.save(newUser);

        return ResponseEntity.ok("Regist sucessful! Welcome " + request.getUsername());
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest request) {
        // 1. Tìm user trong DB
        Optional<User> userOp = userRepository.findByUsername(request.getUsername());

        // 2. Nếu không thấy user
        if (userOp.isEmpty()) {
            return ResponseEntity.badRequest().body("Error: Username does not exit!");
        }

        User user = userOp.get();

        // 3. Kiểm tra password (Lưu ý: Đang so sánh thô, sau này sẽ dùng BCrypt)
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().body("Error: wrong password!");
        }

        // 4. Thành công -> Trả về thông tin user (để Frontend lưu lại)
        return ResponseEntity.ok(user);
    }
    
    // API lấy thông tin user mới nhất (để update coin)
    @GetMapping("/profile/{username}")
    public ResponseEntity<?> getUserProfile(@PathVariable String username) {
        Optional<User> userOp = userRepository.findByUsername(username);
        if (userOp.isPresent()) {
            return ResponseEntity.ok(userOp.get());
        }
        return ResponseEntity.notFound().build();
    }
}