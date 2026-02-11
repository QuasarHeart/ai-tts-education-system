package org.example.fuchuang.service;

import org.example.fuchuang.entity.PptMeta;
import org.example.fuchuang.mapper.PptMetaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;

@Service
public class PptService {

    private final Path pptDir;
    private final PptMetaRepository repo;

    public PptService(@Value("${app.ppt-dir}") String dir, PptMetaRepository repo) {
        this.pptDir = Paths.get(dir).toAbsolutePath().normalize();
        this.repo = repo;
    }

    public PptMeta save(MultipartFile file, String displayName) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file is empty");
        }

        Files.createDirectories(pptDir);

        String id = UUID.randomUUID().toString();
        String originalName = safeFileName(file.getOriginalFilename());
        String ext = getExtension(originalName);
        String storedName = id + ext;

        Path target = pptDir.resolve(storedName).normalize();
        if (!target.startsWith(pptDir)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid path");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        String contentType = guessContentTypeByExtension(ext);

        PptMeta meta = new PptMeta();
        meta.setId(id);
        meta.setOriginalName(originalName);
        meta.setStoredName(storedName);
        meta.setContentType(contentType);
        meta.setSizeBytes(Files.size(target));
        meta.setCreatedAt(Instant.now());
        meta.setDisplayName(normalizeDisplayName(displayName, originalName));

        return repo.save(meta);
    }

    public PptMeta rename(String id, String newName) {
        PptMeta meta = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ppt not found"));

        String n = newName == null ? "" : newName.trim();
        if (n.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name cannot be blank");
        }
        if (n.length() > 80) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name too long (max 80)");
        }

        meta.setDisplayName(n);
        return repo.save(meta);
    }

    public void delete(String id) {
        PptMeta meta = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "ppt not found"));

        Path path = resolvePath(meta);
        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to delete file", e);
        }
        repo.deleteById(id);
    }

    public Path resolvePath(PptMeta meta) {
        Path p = pptDir.resolve(meta.getStoredName()).normalize();
        if (!p.startsWith(pptDir)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid path");
        }
        return p;
    }

    private static String normalizeDisplayName(String displayName, String originalName) {
        String n = displayName == null ? "" : displayName.trim();
        if (!n.isBlank()) return n;

        int i = originalName.lastIndexOf('.');
        return (i > 0) ? originalName.substring(0, i) : originalName;
    }

    private static String safeFileName(String name) {
        if (name == null || name.isBlank()) return "slides";
        return Paths.get(name).getFileName().toString();
    }

    private static String getExtension(String name) {
        int i = name.lastIndexOf('.');
        if (i < 0 || i == name.length() - 1) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "missing file extension (.ppt/.pptx)");
        }
        String ext = name.substring(i).toLowerCase();
        return switch (ext) {
            case ".ppt", ".pptx" -> ext;
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "unsupported extension: " + ext);
        };
    }

    private static String guessContentTypeByExtension(String ext) {
        return switch (ext) {
            case ".ppt" -> "application/vnd.ms-powerpoint";
            case ".pptx" -> "application/vnd.openxmlformats-officedocument.presentationml.presentation";
            default -> "application/octet-stream";
        };
    }
}
