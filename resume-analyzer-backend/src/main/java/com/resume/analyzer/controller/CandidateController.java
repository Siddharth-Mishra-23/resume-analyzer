package com.resume.analyzer.controller;

import com.resume.analyzer.entity.Candidate;
import com.resume.analyzer.service.CandidateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/candidates")
public class CandidateController {

    @Autowired
    private CandidateService candidateService;

    @PostMapping
    public Candidate addCandidate(@RequestBody Candidate candidate) {
    	 System.out.println("Received: " + candidate);
        return candidateService.saveCandidate(candidate);
    }

    @GetMapping
    public List<Candidate> getCandidates() {
        return candidateService.getAllCandidates();
    }
}