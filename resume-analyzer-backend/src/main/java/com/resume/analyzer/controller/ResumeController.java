package com.resume.analyzer.controller;

import com.resume.analyzer.entity.Resume;
import com.resume.analyzer.service.ResumeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/resume")
@CrossOrigin(origins = "http://localhost:8000", allowCredentials = "true")
public class ResumeController {

    @Autowired
    private ResumeService resumeService;

    // ✅ Step 1: Send OTP (form-data)
    @PostMapping("/send-otp")
    public ResponseEntity<String> sendOtp(@RequestParam String name, @RequestParam String email) {
        if (name == null || name.isBlank() || email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Name and Email are required.");
        }

        try {
            Resume resume = resumeService.createOrUpdateWithOtp(name, email);
            return ResponseEntity.ok("OTP sent successfully to " + resume.getEmail());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to send OTP: " + e.getMessage());
        }
    }

    // ✅ Step 2: Verify OTP (form-data)
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        boolean verified = resumeService.verifyOtpByEmail(email, otp);
        return verified
                ? ResponseEntity.ok("OTP verified successfully.")
                : ResponseEntity.status(400).body("Invalid OTP or email.");
    }

    // ✅ Step 3: Upload Resume (multipart/form-data)
    @PostMapping("/upload")
    public ResponseEntity<String> uploadResume(@RequestParam("file") MultipartFile file,
                                               @RequestParam("email") String email) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("Resume file is missing.");
        }
        return resumeService.uploadResume(file, email);
    }

    // ✅ Utility: Get all resumes
    @GetMapping("/all")
    public List<Resume> getAllResumes() {
        return resumeService.getAllResumes();
    }

    // ✅ Step 4A: Skill match percentage (POST, text/plain body)
    @PostMapping("/match-percentage")
    public ResponseEntity<Double> getMatchPercentage(@RequestParam("email") String email,
                                                     @RequestBody String jobDescription) {
        Resume resume = resumeService.getResumeByEmail(email).orElse(null);
        if (resume == null || !resume.isVerified()) {
            return ResponseEntity.badRequest().body(0.0);
        }


        resume.setJobDescription(jobDescription); // 🔑 store JD in resume
        resumeService.saveResume(resume);         // 💾 save it

        double percentage = resumeService.calculateSkillMatchPercentage(email, jobDescription);
        return ResponseEntity.ok(percentage);
    }


    // ✅ Step 4B: Missing keywords (POST, text/plain body)
    @PostMapping("/missing-keywords")
    public ResponseEntity<List<String>> getMissingKeywords(@RequestParam("email") String email,
                                                           @RequestBody String jobDescription) {
        List<String> missing = resumeService.getMissingKeywords(email, jobDescription);
        return ResponseEntity.ok(missing);
    }

    // ✅ Step 4C: Job suggestions (GET)
    @GetMapping("/job-suggestions")
    public ResponseEntity<List<Map<String, String>>> getJobSuggestions(@RequestParam("email") String email) {
        List<Map<String, String>> jobs = resumeService.getJobSuggestions(email);
        return ResponseEntity.ok(jobs);
    }

    // ✅ Step 5: Final summary email (POST)
    @PostMapping("/send-summary-email")
    public ResponseEntity<String> sendFinalResult(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        boolean success = resumeService.sendFinalResultEmail(email);
        return success
                ? ResponseEntity.ok("Final summary email sent to " + email)
                : ResponseEntity.badRequest().body("❌ Failed to send email. Make sure the user is verified and resume is uploaded.");
    }
// ✅ Health check route for Railway root "/"
@GetMapping("/")
public ResponseEntity<String> home() {
    return ResponseEntity.ok("✅ Resume Analyzer backend is running on Railway!");
}

}
