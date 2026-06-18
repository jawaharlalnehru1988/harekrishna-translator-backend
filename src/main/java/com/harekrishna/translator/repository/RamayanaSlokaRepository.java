package com.harekrishna.translator.repository;

import com.harekrishna.translator.model.RamayanaSloka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RamayanaSlokaRepository extends JpaRepository<RamayanaSloka, Long> {
    Optional<RamayanaSloka> findByCantoNumberAndChapterNumberAndVerseNumber(Integer cantoNumber, Integer chapterNumber, Integer verseNumber);
    List<RamayanaSloka> findByCantoNumberAndChapterNumberOrderByVerseNumberAsc(Integer cantoNumber, Integer chapterNumber);
}
