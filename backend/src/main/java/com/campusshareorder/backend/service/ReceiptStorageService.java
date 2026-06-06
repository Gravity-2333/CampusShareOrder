package com.campusshareorder.backend.service;

import com.campusshareorder.backend.common.enums.ErrorCode;
import com.campusshareorder.backend.common.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class ReceiptStorageService {

    private static final long MAX_FILE_SIZE = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");
    private final Path receiptDirectory = Path.of("uploads", "receipts").toAbsolutePath().normalize();

    public String store(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "请选择订单截图");
        }
        if (image.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "文件大小不能超过10MB");
        }

        String originalName = image.getOriginalFilename() == null ? "" : image.getOriginalFilename();
        String extension = getExtension(originalName);
        String contentType = image.getContentType() == null
                ? ""
                : image.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension) || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ErrorCode.VALIDATION_ERROR, "仅支持 JPG/PNG");
        }

        try {
            Files.createDirectories(receiptDirectory);
            String storedName = UUID.randomUUID() + "." + ("jpeg".equals(extension) ? "jpg" : extension);
            Path target = receiptDirectory.resolve(storedName).normalize();
            if (!target.startsWith(receiptDirectory)) {
                throw new BusinessException(ErrorCode.VALIDATION_ERROR, "文件名不合法");
            }
            Files.copy(image.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/receipts/" + storedName;
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "凭证图片保存失败");
        }
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
