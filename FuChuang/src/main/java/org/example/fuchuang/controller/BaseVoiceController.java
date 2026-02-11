package org.example.fuchuang.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class BaseVoiceController {

    @GetMapping("/api/base-voices")
    public List<Map<String, Object>> baseVoices() {
        return List.of(
                Map.of(
                        "code", "zh-CN-standard",
                        "name", "标准普通话",
                        "language", "zh-CN"
                ),
                Map.of(
                        "code", "en-US-standard",
                        "name", "标准英语",
                        "language", "en-US"
                )
        );
    }
}
