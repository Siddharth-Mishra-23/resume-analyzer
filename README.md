# 🧠 Resume Analyzer

An AI-powered web application that analyzes resumes, matches them with job descriptions, and provides personalized job recommendations.

## 🚀 Features

- ✉️ **OTP Email Verification** before resume upload
- 📄 **Resume Parsing** using AI/ML (extracts name, email, phone, skills, etc.)
- ✅ **Skill Matching** with Job Description input
- 📊 **Skill Match Percentage** with circular progress chart
- 💼 **Job Recommendations** using live data from Remotive API
- 📥 Downloadable matched skills in JSON
- 💡 Modern, dark-themed responsive UI

---

## 🌐 Live Demo

Frontend: [resume-analyzer-frontend](https://your-frontend-url)  
Backend: [resume-analyzer-backend](https://your-backend-url)

> ⚠️ Update the links after deployment.

---

## 📁 Project Structure

```bash
resume-analyzer/
│
├── resume-analyzer-frontend/    # HTML, CSS, JS UI
│   ├── index.html
│   ├── style.css
│   └── script.js
│
└── resume-analyzer-backend/     # Spring Boot backend
    ├── src/main/java/com/resume/analyzer
    │   ├── controller/
    │   ├── service/
    │   ├── repository/
    │   └── entity/
    └── src/main/resources/
        └── application.properties
⚙️ Technologies Used
🔧 Backend
Java 17

Spring Boot

Spring Data JPA

MySQL

JavaMailSender

RestTemplate

🎨 Frontend
HTML, CSS, JavaScript

Dark UI with glowing animations

Native JavaScript-based JSON viewer

🤖 AI/ML
Resume parsing

Keyword matching logic

Job suggestions using Remotive.io API


🔐 Environment Variables
Update these in application.properties:
# MySQL Database
spring.datasource.url=jdbc:mysql://localhost:3306/resume_analyzer
spring.datasource.username=root
spring.datasource.password=yourpassword

# Email OTP
spring.mail.username=your@gmail.com
spring.mail.password=your-app-password

# RapidAPI (Remotive)
rapidapi.key=your-rapidapi-key


🧪 Sample Test Flow
Enter your email to receive OTP

Verify OTP to enable resume upload

Upload PDF resume

Paste job description

View skill match %, missing skills, and job recommendations

Download results as JSON
