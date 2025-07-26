package com.resume.analyzer.repository;

import com.resume.analyzer.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    Optional<Resume> findByEmail(String email); // ADD THIS
}
