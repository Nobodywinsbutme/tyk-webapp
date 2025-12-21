package vn.id.tyk.webapp.dto;

import lombok.Builder;
import lombok.Data;
import vn.id.tyk.webapp.entity.DesignSubmission;

import java.time.format.DateTimeFormatter;

@Data
@Builder
public class DesignResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String imageUrl;
    private String category;
    private String status;
    private String creatorName;
    private String createdAt; // Trả về dạng String dd-MM-yyyy cho dễ hiển thị

    public static DesignResponseDTO fromEntity(DesignSubmission design) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        
        return DesignResponseDTO.builder()
                .id(design.getId())
                .title(design.getTitle())
                .description(design.getDescription())
                .imageUrl(design.getImageUrl())
                .category(design.getCategory().name())
                .status(design.getStatus().name())
                .creatorName(design.getCreator().getUsername())
                .createdAt(design.getCreatedAt().format(formatter))
                .build();
    }
}