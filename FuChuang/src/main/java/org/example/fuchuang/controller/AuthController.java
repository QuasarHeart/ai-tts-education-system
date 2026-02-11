package org.example.fuchuang.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.example.fuchuang.dto.ChangeDTO;
import org.example.fuchuang.dto.DeleteDTO;
import org.example.fuchuang.dto.LoginDTO;
import org.example.fuchuang.dto.UserRegisterDTO;
import org.example.fuchuang.dto.EmailRequest;
import org.example.fuchuang.dto.Result;
import org.example.fuchuang.entity.AudioMeta;
import org.example.fuchuang.entity.Ttstask;
import org.example.fuchuang.entity.User;
import org.example.fuchuang.mapper.UserRepository;
import org.example.fuchuang.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import jakarta.annotation.Resource;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @Resource
    private AuthService authService;
    @Resource
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // 发送验证码
    @PostMapping("/code")
    public Result<String> sendCode(@RequestBody EmailRequest request, HttpSession session) {
        String email = request.getEmail();
        // 1. 生成验证码
        String code = authService.generateCode();
        // 2. 存入 Session
        session.setAttribute("code", code);
        session.setAttribute("email", email);
        // 3. 发送邮件
        authService.sendEmailCode(email, code);
        System.out.println("sessionCode:"+code);
        System.out.println("sessionEmail:"+email);
        return Result.success();
    }

    // 注册
    @PostMapping("/register")
    public Result<String> register(@RequestBody UserRegisterDTO dto, HttpSession session) {
        String sessionCode = (String) session.getAttribute("code");
        String sessionEmail = (String) session.getAttribute("email");

        if(userRepository.findByEmail(sessionEmail)!=null){
            return Result.error(402, "用户已存在");
        }
        if (dto.getCode() == null || !dto.getCode().equals(sessionCode)|| !Objects.equals(dto.getEmail(), sessionEmail)) {
            return Result.error(403, "验证码错误");
        }
        //密码限制为6-16位，且只能有数字、字母、下划线，否则返回401
        if (!dto.getPassword().matches("^[a-zA-Z0-9_]{6,16}$")) {
            return Result.error(401, "密码格式错误");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setLoggedIn(false);
        user.setIpAndPort("");
        userRepository.save(user);
        //清除验证码
        session.removeAttribute("code");
        return Result.success();
    }
    // 3. 登录接口
    @PostMapping("/login")
    public Result<String> login(@RequestBody LoginDTO loginDTO, HttpSession session, HttpServletRequest request) {

        String sessionCode = (String) session.getAttribute("code");
        String sessionEmail = (String) session.getAttribute("email");

        if (loginDTO.getCode() == null || !loginDTO.getCode().equals(sessionCode)  || !Objects.equals(loginDTO.getEmail(), sessionEmail)) {
            return Result.error(402, "验证码错误");
        }
        User user = authService.login(loginDTO.getEmail(), loginDTO.getPassword());

        if (user == null)
            return Result.error(401, "用户名或密码错误");
        if(user.isLoggedIn())
            return Result.error(403, "用户已登录");

        // 登录成功，将用户信息存入 Session，表示“已登录”状态
        user.setLoggedIn(true);
        user.setIpAndPort(request.getRemoteAddr() + ":" + request.getLocalPort());
        userRepository.save(user);
        userRepository.flush();

        session.setAttribute("currentUser", user);
        session.setAttribute("userId", user.getId());
        //清除验证码
        session.removeAttribute("code");
        return Result.success();

    }

    // 4. 登出接口
    @GetMapping("/logout")
    public Result<String> logout(HttpSession session) {
        User user = (User)session.getAttribute("currentUser");
        user.setLoggedIn(false);
        userRepository.save(user);
        userRepository.flush();

        session.invalidate(); // 销毁 Session
        return Result.success();
    }
    //5.注销账户
    @PostMapping("/delete")
    public Result<String> delete(@RequestBody DeleteDTO dto, HttpSession session) {
        String sessionCode = (String) session.getAttribute("code");

        if(dto.getCode() == null || !dto.getCode().equals(sessionCode))
            return Result.error(402, "验证码错误");
        User user = (User)session.getAttribute("currentUser");
        //判断密码是否正确，错误返回401
        if(!passwordEncoder.matches(dto.getPassword(), user.getPassword()))
           return Result.error(401, "密码错误");
        try{
        userRepository.delete(user);
        userRepository.flush();
    }catch (Exception e){
            return Result.error(500, "服务器错误");
        }
        session.invalidate();
        return Result.success();
    }
    //6.修改密码
    @PostMapping("/changePassword")
    public Result<String> changePassword(@RequestBody ChangeDTO dto, HttpSession session) {
            User user = (User)session.getAttribute("currentUser");
            String sessionCode = (String) session.getAttribute("code");
            if(!passwordEncoder.matches(dto.getPassword(), user.getPassword()))
                return Result.error(402, "密码错误");
            //密码限制为6-16位，且只能有数字、字母、下划线，否则返回401
            if (!dto.getNewPassword().matches("^[a-zA-Z0-9_]{6,16}$")) {
                return Result.error(401, "密码格式错误");
            }
            //验证码错误
            if (dto.getCode() == null || !dto.getCode().equals(sessionCode))
                return Result.error(403, "验证码错误");
            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            try{
                userRepository.save(user);
                userRepository.flush();
            }catch (Exception e){
                return Result.error(500, "服务器错误");
            }
            //清除验证码
            session.removeAttribute("code");
            session.setAttribute("currentUser", user);
            return Result.success();
    }
    //修改用户昵称
    @PostMapping("/changeNickName")
    public Result<String> changeNickName(@RequestBody ChangeDTO dto, HttpSession session) {
        User user = (User)session.getAttribute("currentUser");
        user.setNickname(dto.getNewNickName());
        try{
            userRepository.save(user);
            userRepository.flush();
        }catch (Exception e){
            return Result.error(500, "服务器错误");
        }
//        session.setAttribute("currentUser", user);
        return Result.success();
    }
    //8.获取用户信息--音频资源
    @GetMapping("/resource/audio")
    public Result<List<AudioMeta>> getUserAudioInfo(HttpSession session) {
        User user = (User)session.getAttribute("currentUser");
        List<AudioMeta> audioMetas = authService.getUserAudioMetas(user.getId());
        return Result.success(audioMetas);
    }
    //9.获取用户信息--任务列表
    @GetMapping("/resource/TtsTasks")
    public Result<List<Ttstask>> getUserTaskInfo(HttpSession session) {
        User user = (User)session.getAttribute("currentUser");
        List<Ttstask> tasks = authService.getUserTtsTasks(user.getId());
        return Result.success(tasks);
    }

}