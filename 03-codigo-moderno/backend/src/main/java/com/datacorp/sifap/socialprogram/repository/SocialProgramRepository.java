package com.datacorp.sifap.socialprogram.repository;

import com.datacorp.sifap.socialprogram.domain.SocialProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SocialProgramRepository extends JpaRepository<SocialProgram, Long> {
    Optional<SocialProgram> findByProgramCode(String programCode);
    boolean existsByProgramCode(String programCode);
}
