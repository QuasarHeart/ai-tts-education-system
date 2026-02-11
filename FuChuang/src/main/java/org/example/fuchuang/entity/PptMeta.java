package org.example.fuchuang.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "ppt_meta")
@Data
public class PptMeta {

    @Id
    private String id;

    private String originalName;
    private String storedName;
    private String contentType;
    private long sizeBytes;
    private Instant createdAt;

    private String displayName;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
