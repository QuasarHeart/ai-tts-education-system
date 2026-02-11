package org.example.fuchuang.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "users")//sql自动创建users表
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    //用户登录状态
    @Column(nullable = false)
    private boolean isLoggedIn;

    //用户当前所在IP和端口
    @Column(nullable = true)
    private String ipAndPort;

    @Column(nullable = true)
    private String nickname;

}