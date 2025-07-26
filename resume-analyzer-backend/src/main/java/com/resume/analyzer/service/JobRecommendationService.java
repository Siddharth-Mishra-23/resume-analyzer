package com.resume.analyzer.service;

import com.resume.analyzer.entity.JobSuggestion;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Service
public class JobRecommendationService {

    @Value("${rapidapi.key}")
    private String apiKey;

    private static final String RAPID_API_HOST = "jsearch.p.rapidapi.com";
    private static final String API_URL = "https://jsearch.p.rapidapi.com/search";

    public List<JobSuggestion> fetchJobsBySkill(String skill) {
        try {
            if (skill == null || skill.isBlank()) return List.of();

            String encodedSkill = URLEncoder.encode(skill.trim(), StandardCharsets.UTF_8);
            URI uri = new URI(API_URL + "?query=" + encodedSkill + "&page=1&num_pages=1");

            HttpHeaders headers = new HttpHeaders();
            headers.set("X-RapidAPI-Key", apiKey);
            headers.set("X-RapidAPI-Host", RAPID_API_HOST);
            headers.setAccept(List.of(MediaType.APPLICATION_JSON));

            HttpEntity<String> entity = new HttpEntity<>(headers);
            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                System.err.println("❌ Failed to fetch jobs for skill: " + skill + " | Status: " + response.getStatusCode());
                return List.of();
            }

            Object dataObj = response.getBody().get("data");
            if (!(dataObj instanceof List)) return List.of();

            List<Map<String, Object>> data = (List<Map<String, Object>>) dataObj;
            if (data.isEmpty()) return List.of();

            Set<JobSuggestion> suggestions = new LinkedHashSet<>();
            for (Map<String, Object> job : data) {
                String title = (String) job.getOrDefault("job_title", "Unknown Title");
                String link = (String) job.getOrDefault("job_apply_link", "");

                if (title != null && !title.isBlank() && link != null && !link.isBlank()) {
                    suggestions.add(new JobSuggestion(title, link));
                }
            }

            return new ArrayList<>(suggestions);

        } catch (Exception e) {
            System.err.println("🔥 Exception while fetching job suggestions for skill: " + skill);
            e.printStackTrace();
            return List.of();
        }
    }
}
