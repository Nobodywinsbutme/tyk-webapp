package vn.id.tyk.webapp.controller;

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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import vn.id.tyk.webapp.dto.LoginRequest;
import vn.id.tyk.webapp.dto.RegisterRequest;
import vn.id.tyk.webapp.entity.User;
import vn.id.tyk.webapp.repository.UserRepository;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // --- THÊM MỚI 1: Công cụ quản lý đăng nhập của Spring Security ---
    @Autowired
    private AuthenticationManager authenticationManager;

    // --- THÊM MỚI 2: Công cụ để lưu Session vào bộ nhớ Server ---
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest request, BindingResult result) {
        // 1. Check lỗi Validation
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body(result.getFieldError().getDefaultMessage());
        }

        // 2. Kiểm tra trùng username
        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest().body("Error: Username exist!");
        }

        // 3. Tạo User mới
        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setEmail(request.getEmail());
        newUser.setCoinBalance(0L);
        newUser.setRole("USER");

        // Mã hóa password
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));

        // Lưu xuống Database
        userRepository.save(newUser);

        return ResponseEntity.ok("Register successful! Welcome " + request.getUsername());
    }

    // --- HÀM LOGIN ĐÃ ĐƯỢC VIẾT LẠI CHUẨN SECURITY ---
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginRequest loginRequest, HttpServletRequest request, HttpServletResponse response) {
        try {
            // 1. Dùng AuthenticationManager để kiểm tra user/pass
            // (Thay vì dùng passwordEncoder.matches thủ công như trước)
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
            );

            // 2. Tạo một Context mới chứa thông tin người vừa đăng nhập thành công
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            // 3. --- QUAN TRỌNG NHẤT: LƯU CONTEXT VÀO SESSION ---
            // Dòng này giúp Server "nhớ" người dùng. Không có dòng này là đăng nhập xong bị quên ngay.
            securityContextRepository.saveContext(context, request, response);

            // 4. Lấy thông tin User trả về cho Frontend (để lưu localStorage)
            User user = userRepository.findByUsername(loginRequest.getUsername()).orElseThrow();
            return ResponseEntity.ok(user);

        } catch (Exception e) {
            // Nếu sai mật khẩu hoặc user không tồn tại, AuthenticationManager sẽ ném lỗi
            return ResponseEntity.badRequest().body("Sai tên đăng nhập hoặc mật khẩu!");
        }
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