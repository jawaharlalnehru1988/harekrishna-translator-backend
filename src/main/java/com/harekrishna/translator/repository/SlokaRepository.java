package com.harekrishna.translator.repository;

import com.harekrishna.translator.model.Sloka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SlokaRepository extends JpaRepository<Sloka, Long> {
    List<Sloka> findByScriptureId(Long scriptureId);
}
