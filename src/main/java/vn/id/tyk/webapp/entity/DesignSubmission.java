package vn.id.tyk.webapp.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "design_submissions")
public class DesignSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // Tên bản thiết kế

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl; // Ảnh demo
    private String fileUrl;  // Link tải file thiết kế (Map/Skin...)

    // Phân loại: MAP, SKIN, WEAPON
    @Enumerated(EnumType.STRING)
    private DesignCategory category;

    // Trạng thái duyệt: PENDING, APPROVED, REJECTED
    @Enumerated(EnumType.STRING)
    private SubmissionStatus status = SubmissionStatus.PENDING;

    // Người tạo
    @ManyToOne
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    private LocalDateTime createdAt = LocalDateTime.now();
    
    // Admin nào duyệt (để tracking)
    @ManyToOne
    @JoinColumn(name = "approver_id")
    private User approvedBy;

    // --- ENUM ---
    public enum DesignCategory {
        MAP, SKIN, WEAPON
    }

    public enum SubmissionStatus {
        PENDING, APPROVED, REJECTED
    }
}