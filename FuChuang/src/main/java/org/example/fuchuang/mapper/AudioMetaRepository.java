package org.example.fuchuang.mapper;

import org.example.fuchuang.entity.AudioMeta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AudioMetaRepository extends JpaRepository<AudioMeta, String> {
    List<AudioMeta> findByUserId(Long userId);
}
