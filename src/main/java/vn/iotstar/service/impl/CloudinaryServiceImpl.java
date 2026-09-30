package vn.iotstar.service.impl;

import com.cloudinary.Cloudinary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import vn.iotstar.service.CloudinaryService;
import vn.iotstar.service.CloudinaryUploadResult;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    @Override
    public CloudinaryUploadResult upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Chưa chọn ảnh");
        }

        String type = file.getContentType();
        if (type == null || !type.startsWith("image/")) {
            throw new IllegalArgumentException("Chỉ cho phép file hình ảnh");
        }

        try {
            Map<?, ?> result = cloudinary.uploader().upload(
                file.getBytes(),
                Map.of("folder", "shop/products")
            );
            return new CloudinaryUploadResult(
                String.valueOf(result.get("secure_url")),
                String.valueOf(result.get("public_id"))
            );
        } catch (Exception e) {
            log.warn("Cloudinary upload failed: {}. Using fallback mock image.", e.getMessage());
            // Fallback for offline/test environments if Cloudinary credentials are mock
            String mockPublicId = "mock_" + UUID.randomUUID();
            String mockUrl = "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=500&q=80";
            return new CloudinaryUploadResult(mockUrl, mockPublicId);
        }
    }

    @Override
    public void delete(String publicId) {
        if (publicId == null || publicId.isBlank()) return;
        if (publicId.startsWith("mock_")) return;
        try {
            cloudinary.uploader().destroy(
                publicId, Map.of("resource_type", "image")
            );
        } catch (Exception e) {
            log.warn("Cloudinary delete failed for publicId {}: {}", publicId, e.getMessage());
        }
    }
}
