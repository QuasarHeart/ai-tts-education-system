package org.example.fuchuang.service;

import jakarta.annotation.Resource;
import org.example.fuchuang.entity.AudioMeta;
import org.example.fuchuang.mapper.AudioMetaRepository;
import org.example.fuchuang.mapper.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.MediaTypeFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class AudioService {

    @Resource
    UserRepository userRepository;

    private final Path audioDir;
    private final AudioMetaRepository repo;

    public AudioService(@Value("${app.audio-dir}") String dir, AudioMetaRepository repo) {
        this.audioDir = Paths.get(dir).toAbsolutePath().normalize();
        this.repo = repo;
    }

    public AudioMeta save(MultipartFile file, Long userId) throws Exception {
        return save(file, null, userId);
    }

    // 支持 displayName
    public AudioMeta save(MultipartFile file, String displayName,Long userId) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "file is empty");
        }

        Files.createDirectories(audioDir);

        String id = UUID.randomUUID().toString();
        String originalName = safeFileName(file.getOriginalFilename());
        String ext = getExtension(originalName);
        String storedName = id + ext;

        Path target = audioDir.resolve(storedName).normalize();
        if (!target.startsWith(audioDir)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "invalid path");
        }

        try (InputStream in = file.getInputStream()) {
            Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
        }

        // contentType：优先用浏览器传来的，不可信时用后缀兜底
        String contentType = file.getContentType();
        String lower = target.getFileName().toString().toLowerCase();

        // 统一用 audio/mp4
        if (lower.endsWith(".m4a")) {
            contentType = "audio/mp4";
        } else if (contentType == null || contentType.isBlank() || "application/octet-stream".equals(contentType)) {
            Optional<MediaType> mt = MediaTypeFactory.getMediaType(target.getFileName().toString());
            contentType = mt.map(MediaType::toString).orElse("application/octet-stream");
        }

        AudioMeta meta = new AudioMeta();
        meta.setId(id);
        meta.setOriginalName(originalName);
        meta.setStoredName(storedName);
        meta.setContentType(contentType);
        meta.setSizeBytes(Files.size(target));
        meta.setCreatedAt(Instant.now());
        meta.setUser(userRepository.getReferenceById(userId));
        // displayName：用户没填则用“原文件名（去后缀）”
        meta.setDisplayName(normalizeDisplayName(displayName, originalName));

        return repo.save(meta);
    }

    // 修改 displayName
    public AudioMeta rename(String id, String newName) {
        AudioMeta meta = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "audio not found"));

        String n = (newName == null) ? "" : newName.trim();
        if (n.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name cannot be blank");
        }
        if (n.length() > 50) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name too long (max 50)");
        }

        meta.setDisplayName(n);
        return repo.save(meta);
    }

    // 删除：删磁盘文件(项目指定目录 ./data/audio) + 删数据库记录
    public void delete(String id) {
        AudioMeta meta = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "audio not found"));

        Path path = resolvePath(meta);
        try {
            Files.deleteIfExists(path);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "failed to delete file", e);
        }

        repo.deleteById(id);
    }

    public Path resolvePath(AudioMeta meta) {
        Path p = audioDir.resolve(meta.getStoredName()).normalize();
        if (!p.startsWith(audioDir)) {
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
        if (name == null || name.isBlank()) return "audio";
        return Paths.get(name).getFileName().toString();
    }

    private static String getExtension(String name) {
        int i = name.lastIndexOf('.');
        if (i < 0 || i == name.length() - 1) return ".bin";
        String ext = name.substring(i).toLowerCase();
        return switch (ext) {
            case ".mp3", ".wav", ".m4a", ".aac", ".ogg", ".webm" -> ext;
            default -> ".bin";
        };
    }
}
