package org.example.fuchuang.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.example.fuchuang.entity.AudioMeta;
import org.example.fuchuang.mapper.AudioMetaRepository;
import org.example.fuchuang.service.AudioService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/audios")
public class AudioController {

    private final AudioService service;
    private final AudioMetaRepository repo;

    public AudioController(AudioService service, AudioMetaRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    //  上传（新增 displayName）
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "displayName", required = false) String displayName,
            HttpSession session
    ) throws Exception {
        AudioMeta meta = service.save(file, displayName,(Long)session.getAttribute("userId"));
        return toDto(meta);
    }

    //  列表（返回 displayName）
    @GetMapping
    public List<Map<String, Object>> list() {
        return repo.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    //  改名
    public record RenameReq(String name) {}

    @PatchMapping("/{id}/name")
    public Map<String, Object> rename(@PathVariable String id, @RequestBody RenameReq req) {
        AudioMeta meta = service.rename(id, req.name());
        return toDto(meta);
    }

    //  删除
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build(); // 204
    }

    //  播放
    @GetMapping("/{id}/stream")
    public void stream(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) throws Exception {
        AudioMeta meta = repo.findById(id).orElse(null);
        if (meta == null) {
            response.setStatus(404);
            return;
        }

        Path path = service.resolvePath(meta);
        if (!Files.exists(path)) {
            response.setStatus(404);
            return;
        }

        long fileLength = Files.size(path);

        // Content-Type：m4a 固定为 audio/mp4
        String ct = meta.getContentType();
        String lowerName = path.getFileName().toString().toLowerCase();
        if (lowerName.endsWith(".m4a")) ct = "audio/mp4";
        if (ct == null || ct.isBlank()) ct = "application/octet-stream";
        response.setContentType(ct);

        response.setHeader("Accept-Ranges", "bytes");

        // 中文文件名：UTF-8，避免 header 异常
        response.setHeader("Content-Disposition",
                ContentDisposition.inline()
                        .filename(meta.getOriginalName(), StandardCharsets.UTF_8)
                        .build()
                        .toString());

        String range = request.getHeader("Range");
        if (range == null || range.isBlank()) {
            response.setStatus(200);
            response.setHeader("Content-Length", String.valueOf(fileLength));
            try (OutputStream out = response.getOutputStream()) {
                Files.copy(path, out);
            }
            return;
        }

        Matcher matcher = Pattern.compile("bytes=(\\d*)-(\\d*)").matcher(range);
        if (!matcher.matches()) {
            response.setStatus(416);
            response.setHeader("Content-Range", "bytes */" + fileLength);
            return;
        }

        long start = matcher.group(1).isEmpty() ? 0 : Long.parseLong(matcher.group(1));
        long end = matcher.group(2).isEmpty() ? (fileLength - 1) : Long.parseLong(matcher.group(2));
        if (end >= fileLength) end = fileLength - 1;
        if (start > end) {
            response.setStatus(416);
            response.setHeader("Content-Range", "bytes */" + fileLength);
            return;
        }

        long chunkSize = end - start + 1;

        response.setStatus(206);
        response.setHeader("Content-Range", "bytes " + start + "-" + end + "/" + fileLength);
        response.setHeader("Content-Length", String.valueOf(chunkSize));

        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "r");
             OutputStream out = response.getOutputStream()) {
            raf.seek(start);
            byte[] buffer = new byte[8192];
            long remaining = chunkSize;
            while (remaining > 0) {
                int read = raf.read(buffer, 0, (int) Math.min(buffer.length, remaining));
                if (read == -1) break;
                out.write(buffer, 0, read);
                remaining -= read;
            }
        }
    }

    //  DTO 输出 
    private Map<String, Object> toDto(AudioMeta m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("name", m.getOriginalName());
        map.put("displayName", m.getDisplayName());   // 允许为 null
        map.put("contentType", m.getContentType());   // 允许为 null
        map.put("sizeBytes", m.getSizeBytes());
        map.put("playUrl", "/api/audios/" + m.getId() + "/stream");
        return map;
    }
}
