package org.example.fuchuang.mapper;

import org.example.fuchuang.entity.Ttstask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TtsTaskRepository extends JpaRepository<Ttstask, Long> {
    Ttstask findByTaskid(String taskid);
    List<Ttstask> findByUseridOrderByCreateTimeDesc(Long userid);
}
