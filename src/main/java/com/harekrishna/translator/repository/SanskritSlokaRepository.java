package com.harekrishna.translator.repository;

import com.harekrishna.translator.model.SanskritSloka;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SanskritSlokaRepository extends JpaRepository<SanskritSloka, Long> {
}
