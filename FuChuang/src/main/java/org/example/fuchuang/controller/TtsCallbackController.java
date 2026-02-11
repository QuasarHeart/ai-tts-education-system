package org.example.fuchuang.controller;
//服务器内部通信

import org.example.fuchuang.service.TtsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/callback")
public class TtsCallbackController {

    @Autowired
    private TtsService ttsService; // 你的业务类

    @PostMapping("/tts")
    public void handlePythonCallback(@RequestBody Map<String, Object> payload) {
        // Python 发过来的 payload 长这样：
        // {"task_id": "...", "status": "success", "audio_url": "..."}

        String taskId = (String) payload.get("task_id");
        String status = (String) payload.get("status");
        String audioUrl = (String) payload.get("audio_url");

        if ("success".equals(status)) {

            System.out.println("任务 " + taskId + " 处理成功，路径：" + audioUrl);
            ttsService.setRestTemplateTaskStatus(taskId, "success",audioUrl);
        }
    }
}