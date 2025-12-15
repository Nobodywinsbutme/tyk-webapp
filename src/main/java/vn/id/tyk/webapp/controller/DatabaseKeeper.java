package vn.id.tyk.webapp.controller;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class DatabaseKeeper {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Cứ 600.000ms (10 phút) thì chạy lệnh này 1 lần
    @Scheduled(fixedRate = 600000) 
    public void keepAlive() {
        try {
            // Lệnh SELECT 1 là lệnh nhẹ nhất, chỉ để báo "Tao còn sống"
            jdbcTemplate.execute("SELECT 1"); 
            System.out.println("💓 Heartbeat: Đã ping Database Aiven để không bị tắt!");
        } catch (Exception e) {
            System.out.println("⚠️ Lỗi ping Database: " + e.getMessage());
        }
    }
}