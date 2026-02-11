package org.example.fuchuang.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.fuchuang.entity.PptMeta;
import org.example.fuchuang.mapper.PptMetaRepository;
import org.example.fuchuang.service.PptService;
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
@RequestMapping("/api/ppts")
public class PptController {

    private final PptService service;
    private final PptMetaRepository repo;

    public PptController(PptService service, PptMetaRepository repo) {
        this.service = service;
        this.repo = repo;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "displayName", required = false) String displayName
    ) throws Exception {
        PptMeta meta = service.save(file, displayName);
        return toDto(meta);
    }

    @GetMapping
    public List<Map<String, Object>> list() {
        return repo.findAll().stream()
                .map(this::toDto)
                .toList();
    }

    public record RenameReq(String name) {}

    @PatchMapping("/{id}/name")
    public Map<String, Object> rename(@PathVariable String id, @RequestBody RenameReq req) {
        PptMeta meta = service.rename(id, req.name());
        return toDto(meta);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/download")
    public void download(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) throws Exception {
        PptMeta meta = repo.findById(id).orElse(null);
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

        String ct = meta.getContentType();
        if (ct == null || ct.isBlank()) ct = "application/octet-stream";
        response.setContentType(ct);
        response.setHeader("Accept-Ranges", "bytes");

        response.setHeader("Content-Disposition",
                ContentDisposition.attachment()
                        .filename(meta.getOriginalName() == null ? "slides.pptx" : meta.getOriginalName(), StandardCharsets.UTF_8)
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

    @GetMapping("/base")
    public List<Map<String, Object>> basePpts() {
        return List.of(
                Map.of(
                        "code", "ppt-zh-template",
                        "name", "基础课件模板（中文）",
                        "desc", "用于快速开始的中文模板",
                        "language", "zh-CN",
                        // 对应 src/main/resources/static/base/ppt-zh-template.pptx
                        "downloadUrl", "/base/ppt-zh-template.pptx"
                ),
                Map.of(
                        "code", "ppt-en-template",
                        "name", "Base Slide Template (English)",
                        "desc", "Starter English template",
                        "language", "en-US",
                        // 对应 src/main/resources/static/base/ppt-en-template.pptx
                        "downloadUrl", "/base/ppt-en-template.pptx"
                )
        );
    }

    private Map<String, Object> toDto(PptMeta m) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", m.getId());
        map.put("name", m.getOriginalName());
        map.put("displayName", m.getDisplayName());
        map.put("contentType", m.getContentType());
        map.put("sizeBytes", m.getSizeBytes());
        map.put("downloadUrl", "/api/ppts/" + m.getId() + "/download");
        return map;
    }
}
