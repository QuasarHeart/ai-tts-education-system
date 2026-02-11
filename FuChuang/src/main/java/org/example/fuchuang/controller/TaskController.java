package org.example.fuchuang.controller;

import jakarta.servlet.http.HttpSession;
import org.example.fuchuang.dto.Result;
import org.example.fuchuang.entity.User;
import org.example.fuchuang.service.TtsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/task")
public class TaskController {
    @Autowired
    private TtsService ttsService;

    @PostMapping("/createTask")
    public Result<String> createByLocalAudio(
            @RequestParam("file") MultipartFile file, // 前端传来的文本文件
            @RequestParam("audioId") String audioId, // 前端传来的音频Id，
            HttpSession session) {

        User user = (User) session.getAttribute("user");
        Long userId = user.getId();

        try {
            // 1. 读取文本内容
            String textContent = new String(file.getBytes(), StandardCharsets.UTF_8);


            // 3. 调用 Service 转发
            String taskId = ttsService.submitTaskToPython(userId, textContent, audioId);

            return Result.success(taskId);
        } catch (Exception e) {
            return Result.error( 401, e.getMessage());
        }
    }
}
