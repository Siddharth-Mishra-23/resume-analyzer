// Use relative path so front + back share the same origin when served by Spring Boot
const baseUrl = "/api/resume";

function sendOTP() {
  const name = document.getElementById("name").value;
  const email = document.getElementById("email").value;

  const formData = new URLSearchParams();
  formData.append("name", name);
  formData.append("email", email);

  const status = document.getElementById("otpStatus");
  status.textContent = "📤 Sending OTP...";

  fetch(`${baseUrl}/send-otp`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: formData.toString()
  })
    .then(response => {
      if (!response.ok) throw new Error("Failed to send OTP");
      return response.text();
    })
    .then(data => {
      status.textContent = "✅ " + data;
      document.getElementById("step1").style.display = "none";
      document.getElementById("step2").style.display = "block";
    })
    .catch(error => {
      status.textContent = "❌ Failed to send OTP";
      console.error("Error:", error);
    });
}

function verifyOTP() {
  const email = document.getElementById("email").value;
  const otp = document.getElementById("otp").value;

  const formData = new URLSearchParams();
  formData.append("email", email);
  formData.append("otp", otp);

  const status = document.getElementById("verifyStatus");
  status.textContent = "🔐 Verifying OTP...";

  fetch(`${baseUrl}/verify-otp`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: formData.toString()
  })
    .then(response => {
      if (!response.ok) throw new Error("Failed to verify OTP");
      return response.text();
    })
    .then(data => {
      status.textContent = "✅ " + data;
      document.getElementById("step2").style.display = "none";
      document.getElementById("step3").style.display = "block";
    })
    .catch(error => {
      status.textContent = "❌ Failed to verify OTP";
      console.error("Error:", error);
    });
}

function uploadResume() {
  const fileInput = document.getElementById("resumeFile");
  const email = document.getElementById("email").value;
  const formData = new FormData();
  if (fileInput.files.length === 0) {
    document.getElementById("uploadStatus").textContent = "❌ Please select a file";
    return;
  }
  formData.append("file", fileInput.files[0]);
  formData.append("email", email);

  const status = document.getElementById("uploadStatus");
  status.textContent = "📤 Uploading resume...";

  fetch(`${baseUrl}/upload`, {
    method: "POST",
    body: formData
  })
    .then(response => {
      if (!response.ok) throw new Error("Failed to upload resume");
      return response.text();
    })
    .then(data => {
      status.textContent = "✅ " + data;
      document.getElementById("step3").style.display = "none";
      document.getElementById("step4").style.display = "block";
    })
    .catch(error => {
      status.textContent = "❌ Failed to upload resume";
      console.error("Error:", error);
    });
}

function analyzeResume() {
  const email = document.getElementById("email").value;
  const jobDescription = document.getElementById("jobDescription").value;
  const analyzeStatus = document.getElementById("analyzeStatus");

  analyzeStatus.textContent = "🧠 Analyzing skills and finding job recommendations...";

  // Match percentage
  fetch(`${baseUrl}/match-percentage?email=${encodeURIComponent(email)}`, {
    method: "POST",
    headers: { "Content-Type": "text/plain" },
    body: jobDescription,
  })
    .then((res) => {
      if (!res.ok) throw new Error("Failed to fetch match percentage");
      return res.json();
    })
    .then((percentage) => {
      const rounded = Math.round(percentage * 100) / 100;
      document.getElementById("matchPercent").innerText = `${rounded}%`;
    })
    .catch((err) => {
      console.error(err);
      document.getElementById("matchPercent").innerText = "Error";
    });

  // Missing keywords
  fetch(`${baseUrl}/missing-keywords?email=${encodeURIComponent(email)}`, {
    method: "POST",
    headers: { "Content-Type": "text/plain" },
    body: jobDescription
  })
    .then(response => {
      if (!response.ok) throw new Error("Failed to fetch missing keywords");
      return response.json();
    })
    .then(missing => {
      const table = document.getElementById("skillsTable");
      table.innerHTML = "<tr><th>Missing Skill</th></tr>";
      missing.forEach(skill => {
        table.innerHTML += `<tr><td>${skill}</td></tr>`;
      });
    })
    .catch(err => {
      console.error("Missing keywords error:", err);
      document.getElementById("skillsTable").innerHTML = "<tr><td>Error loading missing keywords</td></tr>";
    });

  // Job suggestions
  fetch(`${baseUrl}/job-suggestions?email=${encodeURIComponent(email)}`)
    .then(response => {
      if (!response.ok) throw new Error("Failed to fetch job suggestions");
      return response.json();
    })
    .then(jobs => {
      const jobList = document.getElementById("jobList");
      jobList.innerHTML = "";
      if (!jobs || jobs.length === 0) {
        jobList.innerHTML = "<li>No suggestions found.</li>";
      } else {
        jobs.forEach(job => {
          const li = document.createElement("li");
          li.innerHTML = `<a href="${job.url}" target="_blank" rel="noopener noreferrer">${job.title}</a>`;
          jobList.appendChild(li);
        });
      }

      // Wait glitter message
      const finalMailStatus = document.getElementById("finalMailStatus");
      finalMailStatus.innerHTML = `<p class="glitter-text">⏳ Generating final summary report, please wait...</p>`;

      // Final summary email
      fetch(`${baseUrl}/send-summary-email`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ email: email })
      })
        .then(res => {
          if (!res.ok) throw new Error("Failed to send summary email");
          return res.text();
        })
        .then(msg => {
          finalMailStatus.innerHTML = `<p class="glitter-text">📧 ${msg}</p>`;
        })
        .catch(err => {
          finalMailStatus.innerHTML = `<p class="glitter-text">⚠️ Could not send summary email.</p>`;
          console.error("Summary email error:", err);
        });

      // Show results
      analyzeStatus.textContent = "";
      document.getElementById("step4").style.display = "none";
      document.getElementById("step5").style.display = "block";
    })
    .catch(error => {
      analyzeStatus.textContent = "❌ Error during job analysis.";
      console.error("Job suggestions error:", error);
    });
}
