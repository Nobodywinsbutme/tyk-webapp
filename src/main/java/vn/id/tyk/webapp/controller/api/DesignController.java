package vn.id.tyk.webapp.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import vn.id.tyk.webapp.dto.DesignResponse;
import vn.id.tyk.webapp.service.DesignService;

import java.util.List;

@RestController
@RequestMapping("/api/designs")
public class DesignController {

    @Autowired
    private DesignService designService;

    // 1. Upload bài mới
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadDesign(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("category") String category,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        try {
            DesignResponse response = designService.createDesign(
                title, description, category, image, userDetails.getUsername()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error uploading design: " + e.getMessage());
        }
    }

    // 2. Lấy danh sách My Designs
    @GetMapping("/my-designs")
    public ResponseEntity<List<DesignResponse>> getMyDesigns(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(designService.getMyDesigns(userDetails.getUsername()));
    }

    // 3. Lấy danh sách Public (Ai cũng xem được)
    @GetMapping("/public")
    public ResponseEntity<List<DesignResponse>> getPublicDesigns() {
        return ResponseEntity.ok(designService.getPublicDesigns());
    }

    // 4. Xóa bài viết
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDesign(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {
        try {
            designService.deleteDesign(id, userDetails.getUsername());
            return ResponseEntity.ok("Successfully deleted the design.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error deleting design: " + e.getMessage());
        }
    }

    // 5. Cập nhật bài viết
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateDesign(
            @PathVariable Long id,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "image", required = false) MultipartFile image,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            DesignResponse response = designService.updateDesign(
                id, title, description, image, userDetails.getUsername()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error updating design: " + e.getMessage());
        }
    }

    // --- ADMIN API ---

    // 6. Lấy danh sách chờ duyệt (Pending)
    @GetMapping("/pending")
    public ResponseEntity<List<DesignResponse>> getPendingDesigns() {
        return ResponseEntity.ok(designService.getPendingDesigns());
    }

    // 7. Duyệt / Từ chối bài
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Long id,
            @RequestParam("status") String status,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            designService.changeStatus(id, status, userDetails.getUsername());
            return ResponseEntity.ok("Successfully updated status!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error processing: " + e.getMessage());
        }
    }
}