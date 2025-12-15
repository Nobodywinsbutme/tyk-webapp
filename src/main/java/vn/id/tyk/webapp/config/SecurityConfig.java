package vn.id.tyk.webapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // Dùng thuật toán BCrypt để hash
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Tắt CSRF để gọi API dễ dàng hơn
            .authorizeHttpRequests(auth -> auth
                // Cho phép tất cả mọi người truy cập các link này:
                .requestMatchers(
                "/", 
                "/marketplace", // Cho phép vào chợ
                "/news",        // Cho phép xem tin tức
                "/guide",       // Cho phép xem hướng dẫn
                "/status",      // Cho phép xem status
                "/api/auth/**", 
                "/css/**", "/js/**", "/img/**"
            ).permitAll()
                .anyRequest().authenticated() // Các link khác phải đăng nhập
            )
            // Cấu hình Login (Giữ nguyên hoặc chỉnh sửa tùy code cũ)
            .formLogin(form -> form
                .loginPage("/") 
                .permitAll()
            )

            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessUrl("/")
                .permitAll()
            )
            // --- THÊM ĐOẠN NÀY ---
            .rememberMe(remember -> remember
                .key("khongchodaudungcohoi") // Key để mã hóa cookie
                .tokenValiditySeconds(7 * 24 * 60 * 60) // Cookie sống 7 ngày
                .rememberMeParameter("remember-me") // Tên của ô checkbox ở HTML
            );
            // ---------------------
        return http.build();
    }
}