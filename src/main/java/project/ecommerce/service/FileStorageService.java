package project.ecommerce.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload.dir}")
    private String uploadDir;

    public String save(MultipartFile file) {
        validateFile(file);

        try {
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String extension = getExtension(file.getOriginalFilename());
            String filename = UUID.randomUUID() + "." + extension;
            Path filePath = uploadPath.resolve(filename);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return "/api/images/" + filename;

        } catch (IOException e) {
            throw new RuntimeException("Erro ao salvar o arquivo: " + e.getMessage());
        }
    }

    public void delete(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/api/images/")) return;

        try {
            String filename = imageUrl.replace("/api/images/", "");
            Path filePath = Paths.get(uploadDir).resolve(filename);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Erro ao deletar o arquivo: " + e.getMessage());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("Arquivo vazio ou inválido.");
        }

        String extension = getExtension(file.getOriginalFilename());
        if (!extension.matches("jpg|jpeg|png|webp")) {
            throw new RuntimeException("Formato inválido. Use jpg, jpeg, png ou webp.");
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new RuntimeException("Nome de arquivo inválido.");
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }
}
