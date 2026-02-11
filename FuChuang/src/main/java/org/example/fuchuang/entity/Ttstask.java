package org.example.fuchuang.entity;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "Ttstasks")
public class Ttstask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 逻辑关联：多个任务可以由同一个用户创建
    @ManyToOne
    @JoinColumn(name = "user_id") // 对应数据库的外键列名
    private User user;

    // 逻辑关联：多个任务可以使用同一个音色模板
    @ManyToOne
    @JoinColumn(name = "voice_id") // 对应数据库的外键列名
    private AudioMeta voice;

    @Column(unique = true)
    private String taskid;
    @Column
    private String status;
    @Column(unique = true)
    private String result_path;
    @Column
    private String time;
}
