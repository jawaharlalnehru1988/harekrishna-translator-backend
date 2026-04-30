package com.harekrishna.translator.repository;

import com.harekrishna.translator.model.Glossary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GlossaryRepository extends JpaRepository<Glossary, Long> {
    Optional<Glossary> findBySourceWord(String sourceWord);
}
