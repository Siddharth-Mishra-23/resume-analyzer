package com.resume.analyzer.controller;

import com.resume.analyzer.entity.Candidate;
import com.resume.analyzer.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid; // ✅ CORRECT

import java.util.List;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    @PostMapping
    public ResponseEntity<?> addCandidate(@Valid @RequestBody Candidate candidate, BindingResult result) {
        if (result.hasErrors()) {
            return ResponseEntity.badRequest().body("❌ Invalid input: Please check name and email.");
        }

        System.out.println("Received: " + candidate);
        Candidate savedCandidate = candidateService.saveCandidate(candidate);
        return ResponseEntity.ok(savedCandidate);
    }

    @GetMapping
    public List<Candidate> getCandidates() {
        return candidateService.getAllCandidates();
    }
}
