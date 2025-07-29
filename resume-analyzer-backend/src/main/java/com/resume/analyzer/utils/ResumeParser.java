package com.resume.analyzer.utils;

import org.apache.tika.Tika;

import java.io.InputStream;

public class ResumeParser {

    private static final Tika tika = new Tika();

    public static String extractText(InputStream inputStream) {
        try {
            return tika.parseToString(inputStream);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
