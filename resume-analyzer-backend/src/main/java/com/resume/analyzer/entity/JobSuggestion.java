package com.resume.analyzer.entity;

public class JobSuggestion {
    private String title;
    private String url;

    public JobSuggestion() {}

    public JobSuggestion(String title, String url) {
        this.title = title;
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
