package org.example.fuchuang.mapper;

import org.example.fuchuang.entity.PptMeta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PptMetaRepository extends JpaRepository<PptMeta, String> {
    List<PptMeta> findByUserId(Long userId);
}
