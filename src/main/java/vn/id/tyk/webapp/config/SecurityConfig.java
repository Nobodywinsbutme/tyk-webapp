package vn.id.tyk.webapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import vn.id.tyk.webapp.repository.UserRepository;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Dùng thuật toán BCrypt để hash
    }

    // Bean này giúp AuthController có thể gọi được chức năng đăng nhập
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return username -> userRepository.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPassword())
                        .roles(user.getRole()) 
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) 
            .authorizeHttpRequests(auth -> auth
                // 1. CÁC TRANG CÔNG KHAI (HTML VIEWS & PUBLIC API)
                .requestMatchers(
                    "/", 
                    "/marketplace", 
                    "/news",        
                    "/guide",       
                    "/community",   // Trang cộng đồng
                    "/my-designs",  // Trang quản lý (Chỉ là vỏ HTML -> Cho phép vào để JS tự kiểm tra)
                    "/api/designs/public", // API lấy danh sách bài public
                    "/uploads/**",
                    "/api/auth/**", 
                    "/api/news/**",
                    "/css/**", "/js/**", "/img/**"
                ).permitAll()
                
                .requestMatchers(
                    "/admin", 
                    "/api/designs/pending", 
                    "/api/designs/*/status"
                ).hasRole("ADMIN")

                // 2. CÁC API CẦN BẢO MẬT (Dữ liệu nhạy cảm)
                // Các API bắt đầu bằng /api/designs/ (trừ cái public ở trên) đều bắt buộc đăng nhập
                // Bao gồm: /api/designs/my-designs, /api/designs/upload, DELETE, PUT...
                .requestMatchers(
                    "/api/designs/**",
                    "/admin"
                ).authenticated()
                
                // 3. CÁC LINK KHÁC
                .anyRequest().authenticated()
            )
            
            // Cấu hình Login
            .formLogin(form -> form
                .loginPage("/") 
                .permitAll()
            )

            // Cấu hình Logout
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            )
            
            // Cấu hình Remember Me
            .rememberMe(remember -> remember
                .key("khongchodaudungcohoi") 
                .tokenValiditySeconds(7 * 24 * 60 * 60) 
                .rememberMeParameter("remember-me") 
            );
            
        return http.build();
    }
}