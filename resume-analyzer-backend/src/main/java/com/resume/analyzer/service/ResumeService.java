package com.resume.analyzer.service;

import com.resume.analyzer.entity.JobSuggestion;
import com.resume.analyzer.entity.Resume;
import com.resume.analyzer.repository.ResumeRepository;
import com.resume.analyzer.utils.ResumeParser;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class ResumeService {

    private static final String UPLOAD_DIR = "D:/java ee/resume-analyzer-backend/uploads";

    @Autowired
    private ResumeRepository repository;

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private MailService mailService;

    @Autowired
    private JobRecommendationService jobRecommendationService;

    private final Tika tika = new Tika();

    private static final List<String> knownSkills = List.of(
            "Java", "Python", "C", "C++", "C#", "JavaScript", "TypeScript", "Go", "Rust", "Ruby", "PHP", "Kotlin", "Swift", "R", "Scala",
            "Spring", "Spring Boot", "Spring MVC", "Spring Security", "Spring Cloud", "Hibernate", "JPA",
            "Django", "Flask", "Express", "Node.js", "ASP.NET", "Laravel", "NestJS",
            "HTML", "CSS", "React", "Angular", "Vue.js", "Svelte", "Next.js", "Nuxt.js", "Tailwind CSS", "Bootstrap", "Material UI", "jQuery",
            "Android", "Java (Android)", "React Native", "Flutter", "Dart", "iOS", "Jetpack Compose",
            "MySQL", "PostgreSQL", "MongoDB", "Oracle", "SQL Server", "SQLite", "MariaDB", "Cassandra", "Redis", "DynamoDB", "Elasticsearch", "NoSQL", "SQL", "InfluxDB", "Neo4j",
            "Docker", "Kubernetes", "Helm", "Terraform", "Ansible", "Chef", "Puppet",
            "Git", "GitHub", "GitLab", "Bitbucket", "Jenkins", "CircleCI", "Travis CI", "GitLab CI", "GitHub Actions", "CI/CD", "ArgoCD", "Spinnaker",
            "AWS", "Azure", "GCP", "S3", "EC2", "Lambda", "RDS", "ECS", "EKS", "CloudWatch", "Firebase", "Netlify", "Heroku", "Vercel",
            "Kafka", "RabbitMQ", "ActiveMQ", "SQS", "Pub/Sub", "Event-Driven Architecture",
            "JUnit", "Mockito", "TestNG", "Selenium", "Cypress", "Playwright", "Puppeteer", "Rest Assured", "JMeter", "Postman",
            "Prometheus", "Grafana", "ELK Stack", "Logstash", "Kibana", "Fluentd", "New Relic", "Datadog", "Sentry", "Graylog",
            "TensorFlow", "PyTorch", "Keras", "Scikit-learn", "Pandas", "NumPy", "Matplotlib", "Seaborn", "OpenCV",
            "Hugging Face", "BERT", "Transformers", "XGBoost", "LightGBM", "LangChain", "LLMs", "RAG", "Prompt Engineering",
            "MLFlow", "ONNX", "Google Colab", "Jupyter", "AutoML",
            "Apache Hadoop", "Apache Spark", "Kafka Streams", "Airflow", "DBT", "Flink", "Presto", "Hive", "HBase",
            "ETL", "Data Warehousing", "Snowflake", "BigQuery", "Redshift", "Data Lakes", "Athena",
            "REST APIs", "GraphQL", "gRPC", "WebSockets", "OpenAPI", "Swagger",
            "OAuth2", "JWT", "SSO", "SAML", "OpenID Connect", "SSL", "TLS", "IAM", "Firewalls", "OWASP", "Zero Trust",
            "System Design", "Low-level Design", "High-level Design", "Design Patterns", "OOP", "MVC", "DDD", "TDD", "SOLID Principles",
            "VS Code", "IntelliJ IDEA", "Eclipse", "Notion", "Slack", "Zoom", "Jira", "Confluence", "Trello", "Figma", "Adobe XD",
            "Bash", "Shell Scripting", "Linux", "PowerShell", "JSON", "XML", "YAML", "Protobuf", "Thrift"
        );

    private static final Set<String> ignoreWords = Set.of(
            "a", "an", "and", "are", "as", "at", "be", "been", "but", "by", "for", "from", "has", "have", "if", "in",
            "into", "is", "it", "its", "not", "of", "on", "or", "so", "such", "than", "that", "the", "their", "these",
            "this", "to", "was", "we", "were", "will", "with", "you", "your", "they", "he", "she", "i", "my", "me",
            "our", "apply", "available", "build", "click", "contact", "details", "download", "find", "follow", "get",
            "go", "help", "join", "know", "learn", "make", "note", "open", "read", "refer", "register", "see", "send",
            "start", "submit", "support", "use", "using", "visit", "wait", "work", "write", "upon", "inr", "lakhs",
            "annum", "month", "months", "year", "years", "type", "types", "case", "cases", "project", "projects",
            "student", "students", "job", "role", "position", "profile", "company", "organization", "tech",
            "technology", "technologies", "solution", "solutions", "team", "members", "environment", "tools", "tool",
            "platform", "product", "field", "area", "internship", "2023", "2024", "2025", "2026"
        );

    // 🔹 Get all resumes
    public List<Resume> getAllResumes() {
        return repository.findAll();
    }

    // 🔹 Save resume directly
    public Resume saveResume(Resume resume) {
        return repository.save(resume);
    }

    // 🔹 Generate OTP and send email
    public Resume createOrUpdateWithOtp(String name, String email) {
        Resume resume = repository.findByEmail(email).orElse(new Resume());
        resume.setName(name);
        resume.setEmail(email);
        resume.setOtp(generateOtp());
        resume.setVerified(false);
        repository.save(resume);
        mailService.sendOtpEmail(email, resume.getOtp());
        return resume;
    }

    // 🔹 Verify OTP
    public boolean verifyOtpByEmail(String email, String userOtp) {
        return repository.findByEmail(email).map(resume -> {
            if (resume.getOtp() != null && resume.getOtp().equals(userOtp)) {
                resume.setVerified(true);
                resume.setOtp(null);
                repository.save(resume);
                return true;
            }
            return false;
        }).orElse(false);
    }

    // 🔹 Resume upload and parsing
    public ResponseEntity<String> uploadResume(MultipartFile file, String email) {
        Optional<Resume> optionalResume = repository.findByEmail(email);
        if (optionalResume.isEmpty() || !optionalResume.get().isVerified()) {
            return ResponseEntity.status(403).body("Email not verified. Please complete OTP verification first.");
        }

        try {
            byte[] fileBytes = file.getBytes();
            String extractedText = ResumeParser.extractText(file.getInputStream());

            String extractedEmail = extractEmail(extractedText);
            String extractedPhone = extractPhone(extractedText);

            List<String> matchedSkills = knownSkills.stream()
                    .filter(skill -> extractedText.toLowerCase().contains(skill.toLowerCase()))
                    .distinct()
                    .collect(Collectors.toList());
            
            System.out.println("🛠 Extracted Skills from Resume: " + matchedSkills);

            // ✅ Ensure uploads directory exists
 	    File uploadDir = new File(UPLOAD_DIR);
	    if (!uploadDir.exists()) {
  	    uploadDir.mkdirs();
	    }
            Path filePath = Paths.get(UPLOAD_DIR, file.getOriginalFilename());
            file.transferTo(filePath.toFile());

            Resume resume = optionalResume.get();
            resume.setPhone(extractedPhone != null ? extractedPhone : "Not Found");
            resume.setSkills(!matchedSkills.isEmpty() ? String.join(", ", matchedSkills) : "Not Found");
            resume.setFilePath(filePath.toString());
            if (resume.getJobDescription() == null) {
                resume.setJobDescription("Not provided");
            }

            repository.save(resume);
            return ResponseEntity.ok("Resume uploaded and parsed successfully.");

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Failed to upload or parse resume.");
        }
    }

    // 🔹 Calculate skill match % between resume and JD
    public double calculateSkillMatchPercentage(String email, String jobDescription) {
        Optional<Resume> optionalResume = repository.findByEmail(email);
        if (optionalResume.isEmpty() || !optionalResume.get().isVerified()) return 0.0;

        Resume resume = optionalResume.get();
        resume.setJobDescription(jobDescription);
        repository.save(resume);

        String resumeSkills = resume.getSkills();
        if (resumeSkills == null || resumeSkills.equalsIgnoreCase("Not Found")) return 0.0;

        Set<String> resumeSkillsSet = Arrays.stream(resumeSkills.split(",\\s*"))
                .map(String::toLowerCase).collect(Collectors.toSet());
        
        System.out.println("📄 Resume Skills: " + resumeSkillsSet);


        // Tokenize JD text into individual meaningful words
        Set<String> jdWords = Arrays.stream(jobDescription.toLowerCase().split("[^a-zA-Z0-9+#.\\-]"))
                .filter(word -> word.length() > 1 && !ignoreWords.contains(word))
                .collect(Collectors.toSet());
        
        System.out.println("📝 JD Words: " + jdWords);


        // For multi-word skills, match full phrase in JD; else match by presence in jdWords
        long matched = resumeSkillsSet.stream().filter(skill -> {
            String lower = skill.toLowerCase();
            return jobDescription.toLowerCase().contains(lower) || jdWords.contains(lower);
        }).count();

    	System.out.println("✅ Matched Skills Count: " + matched);
    	System.out.println("📊 Match Percentage: " + (matched * 100.0 / resumeSkillsSet.size()));

        return resumeSkillsSet.isEmpty() ? 0.0 :
                Math.round((matched * 100.0 / resumeSkillsSet.size()) * 100.0) / 100.0;
    }

    // 🔹 Find missing but relevant keywords
    public List<String> getMissingKeywords(String email, String jobDescription) {
        Optional<Resume> optionalResume = repository.findByEmail(email);
        if (optionalResume.isEmpty() || !optionalResume.get().isVerified()) return Collections.emptyList();

        Resume resume = optionalResume.get();
        String resumeSkills = resume.getSkills();
        if (resumeSkills == null || resumeSkills.equalsIgnoreCase("Not Found")) {
            return Collections.emptyList();
        }

        Set<String> resumeSkillsSet = Arrays.stream(resumeSkills.split(",\\s*"))
                .map(String::toLowerCase).collect(Collectors.toSet());

        Set<String> jdWords = Arrays.stream(jobDescription.toLowerCase().split("[^a-zA-Z0-9+#.\\-]"))
                .filter(w -> w.length() > 2 && !ignoreWords.contains(w))
                .collect(Collectors.toSet());

        return knownSkills.stream()
                .filter(skill -> {
                    String lower = skill.toLowerCase();
                    return lower.contains(" ") ? jobDescription.toLowerCase().contains(lower) : jdWords.contains(lower);
                })
                .filter(skill -> !resumeSkillsSet.contains(skill.toLowerCase()))
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    // 🔹 Suggest jobs using external API
    public List<Map<String, String>> getJobSuggestions(String email) {
        Optional<Resume> optionalResume = repository.findByEmail(email);
        if (optionalResume.isEmpty() || !optionalResume.get().isVerified()) return Collections.emptyList();

        Resume resume = optionalResume.get();
        String resumeSkills = resume.getSkills();
        if (resumeSkills == null || resumeSkills.equalsIgnoreCase("Not Found")) {
            return Collections.emptyList();
        }

        List<String> topSkills = Arrays.stream(resumeSkills.split(",\\s*"))
                .map(String::toLowerCase)
                .distinct()
                .limit(3)
                .toList();

        List<Map<String, String>> jobResults = new ArrayList<>();
        for (String skill : topSkills) {
            List<JobSuggestion> suggestions = jobRecommendationService.fetchJobsBySkill(skill);
            if (!suggestions.isEmpty()) {
                JobSuggestion suggestion = suggestions.get(0);
                Map<String, String> jobMap = new HashMap<>();
                jobMap.put("title", suggestion.getTitle());
                jobMap.put("url", suggestion.getUrl());
                jobMap.put("matchSkill", skill);
                jobResults.add(jobMap);
            }
        }

        return jobResults;
    }

    // 🔹 Send final result email to user
    public boolean sendFinalResultEmail(String email) {
        Optional<Resume> optionalResume = repository.findByEmail(email);
        if (optionalResume.isEmpty()) return false;

        Resume resume = optionalResume.get();
        if (!resume.isVerified()) return false;

        String extractedSkills = resume.getSkills();
        String jobDescription = resume.getJobDescription();
        if (extractedSkills == null || jobDescription == null) return false;

        List<String> resumeSkillList = Arrays.stream(extractedSkills.split(","))
                .map(String::trim).map(String::toLowerCase).toList();

        List<String> matchedSkillsList = resumeSkillList.stream()
                .filter(jobDescription.toLowerCase()::contains)
                .distinct().toList();

        String matchedSkills = String.join(", ", matchedSkillsList);
        double matchPercent = resumeSkillList.isEmpty() ? 0 :
                (matchedSkillsList.size() * 100.0) / resumeSkillList.size();

        List<Map<String, String>> jobs = getJobSuggestions(email);
        StringBuilder jobLinks = new StringBuilder();
        for (Map<String, String> job : jobs) {
            jobLinks.append("<li><a href=\"").append(job.get("url")).append("\">")
                    .append(job.get("title")).append("</a></li>");
        }

        String htmlContent = "<h2>🎯 Resume Match Summary</h2>"
                + "<p><b>Extracted Skills:</b> " + extractedSkills + "</p>"
                + "<p><b>Matched Skills:</b> " + matchedSkills + "</p>"
                + "<p><b>Match Percentage:</b> " + String.format("%.2f", matchPercent) + "%</p>"
                + "<p><b>Top Job Recommendations:</b></p><ul>" + jobLinks + "</ul>";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("📊 Your Resume Analysis Summary");
            helper.setText(htmlContent, true);
            mailSender.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 🔹 Fetch resume by email
    public Optional<Resume> getResumeByEmail(String email) {
        return repository.findByEmail(email);
    }

    // 🔹 Utility: Extract email from text
    private String extractEmail(String text) {
        Matcher matcher = Pattern.compile("\\b[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,6}\\b").matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    // 🔹 Utility: Extract phone number from text
    private String extractPhone(String text) {
        Matcher matcher = Pattern.compile("\\+?\\d{1,3}?[-.\\s]?\\(?\\d{3}\\)?[-.\\s]?\\d{3}[-.\\s]?\\d{4}").matcher(text);
        return matcher.find() ? matcher.group() : null;
    }

    // 🔹 Utility: Generate 6-digit OTP
    private String generateOtp() {
        return String.valueOf(new Random().nextInt(900000) + 100000);
    }
}
