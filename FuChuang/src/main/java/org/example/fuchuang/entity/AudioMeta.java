package org.example.fuchuang.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "audio_meta")
@Data
public class AudioMeta {

    @Id
    private String id;
    private String originalName;      // 用户上传的音频名称
    private String storedName;        // 存储在本地的文件名称
    private String contentType;       // 音频类型
    private long sizeBytes;
    private Instant createdAt;
    private String displayName;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
