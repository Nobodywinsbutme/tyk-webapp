package vn.id.tyk.webapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_reports")
public class UserReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Người đi tố cáo (Dân thường)
    @ManyToOne
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    // Người bị tố cáo (Kẻ gian)
    @ManyToOne
    @JoinColumn(name = "reported_user_id", nullable = false)
    private User reportedUser;

    @Column(columnDefinition = "TEXT")
    private String reason; // Lý do tố cáo (Lừa đảo, chửi bậy...)

    // Trạng thái xử lý: PENDING (Chờ), RESOLVED (Đã xử lý - Ban), DISMISSED (Bác bỏ - Vô tội)
    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.PENDING;

    private LocalDateTime createdAt = LocalDateTime.now();

    // Ghi chú của Admin khi xử lý (ví dụ: "Đã ban 7 ngày")
    private String adminNote;

    public enum ReportStatus {
        PENDING, RESOLVED, DISMISSED
    }
}