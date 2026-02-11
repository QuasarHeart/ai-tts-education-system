package org.example.fuchuang.service;

import jakarta.annotation.Resource;

import org.example.fuchuang.entity.AudioMeta;
import org.example.fuchuang.entity.Ttstask;
import org.example.fuchuang.entity.User;
import org.example.fuchuang.mapper.AudioMetaRepository;
import org.example.fuchuang.mapper.TtsTaskRepository;
import org.example.fuchuang.mapper.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class AuthService {

    @Resource
    private JavaMailSender mailSender;
    @Resource
    private UserRepository userRepository;
    @Resource
    private AudioMetaRepository audiometaRepository;
    @Resource
    private TtsTaskRepository ttsTaskRepository;
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // 发送 QQ 邮件逻辑
    public void sendEmailCode(String toEmail, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("2979734778@qq.com");
        message.setTo(toEmail);
        message.setSubject("校园创赛注册验证码");
        message.setText("您的验证码为：" + code + "，请在5分钟内完成注册。");
        mailSender.send(message);
    }

    // 生成6位随机验证码
    public String generateCode() {
        return String.valueOf(new Random().nextInt(899999) + 100000);
    }
    /**
     * 登录业务逻辑
     * @return 返回登录成功的用户信息，失败则返回 null 或抛出异常
     * password 参数是明文
     */
    public User login(String email, String password) {
        // 1. 根据邮箱查找用户
        User user = userRepository.findByEmail(email);
        // 2. 校验用户是否存在且密码正确
        if (passwordEncoder.matches(password, user.getPassword())) {
            System.out.println("密码正确");
            return user;
        }
        return null;
    }
    public List<AudioMeta> getUserAudioMetas(Long userId) {
        List<AudioMeta> metas = audiometaRepository.findByUserId(userId);
        return metas;
    }
    public List<Ttstask> getUserTtsTasks(Long userId) {
        List<Ttstask> tasks = ttsTaskRepository.findByUseridOrderByCreateTimeDesc(userId);
        return tasks;
    }
}