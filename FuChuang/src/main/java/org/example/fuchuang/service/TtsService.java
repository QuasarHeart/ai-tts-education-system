package org.example.fuchuang.service;

import org.example.fuchuang.entity.AudioMeta;
import org.example.fuchuang.entity.Ttstask;
import org.example.fuchuang.entity.User;
import org.example.fuchuang.mapper.AudioMetaRepository;
import org.example.fuchuang.mapper.TtsTaskRepository;
import org.example.fuchuang.mapper.UserRepository;
import org.hibernate.cfg.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
public class TtsService {
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    TtsTaskRepository ttsTaskRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    AudioMetaRepository audioMetaRepository;

    private static final String pythonUrl = "http://localhost:8000/api/tts/async";

    // 提交任务给 Python,设置任务调用者，音频文件名，文本文件
    public String submitTaskToPython(Long userId,String text, String voiceId) {

        AudioMeta audioMeta = audioMetaRepository.getReferenceById(voiceId);
        String filePath = "./data/audio/"+ audioMeta.getStoredName();

        // 1. 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        // 2. 构造表单内容 (对应 Python 里的每一个 Form 和 File 参数)
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("text", text);               // 对应 text: str = Form(...)
        body.add("text_lang", "zh");          // 对应 text_lang: str = Form("zh")
        body.add("prompt_lang", "zh");
        body.add("format", "mp3");
        body.add("ref_audio", filePath); // 对应 ref_audio: UploadFile = File(...)

        body.add("callback_url", "http://localhost:8080/api/v1/callback/tts");

        // 3. 发送 POST 请求
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        // 接收 Python 返回的 {"task_id": "...", "status": "..."}
        Map<String, String> response = restTemplate.postForObject(pythonUrl, requestEntity, Map.class);

        setRestTemplateTaskStatus(response.get("task_id"), response.get("status"), response.get("audio_url"), userId,voiceId);
        return response.get("task_id");
    }
    //设置数据库信息
    public void setRestTemplateTaskStatus(String taskId, String status, String audioUrl, Long userId, String  voiceId) {
        try {
            User user = userRepository.getReferenceById(userId);

            Ttstask ttstask;
            if(ttsTaskRepository.findByTaskid(taskId)== null){
                 ttstask = new Ttstask();
            }else {
                 ttstask = ttsTaskRepository.findByTaskid(taskId);
            }
            ttstask.setTaskid(taskId);
            ttstask.setStatus(status);
            ttstask.setResult_path(audioUrl);
            ttstask.setUser(user);
            ttstask.setVoice(audioMetaRepository.getReferenceById(voiceId));
            ttsTaskRepository.save(ttstask);
        }catch (Exception e){
            System.out.println("数据库错误");
        }
    }
    //
    public void setRestTemplateTaskStatus(String taskId, String status, String audioUrl) {
        try {
            Ttstask ttstask;
            if(ttsTaskRepository.findByTaskid(taskId)== null){
                ttstask = new Ttstask();
            }else {
                ttstask = ttsTaskRepository.findByTaskid(taskId);
            }
            ttstask.setTaskid(taskId);
            ttstask.setStatus(status);
            ttstask.setResult_path(audioUrl);
            ttsTaskRepository.save(ttstask);
        }catch (Exception e){
            System.out.println("数据库错误");
        }
    }
}