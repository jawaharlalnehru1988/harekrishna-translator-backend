package com.harekrishna.translator.repository;

import com.harekrishna.translator.model.Scripture;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ScriptureRepository extends JpaRepository<Scripture, Long> {
    Optional<Scripture> findByTitle(String title);
}
