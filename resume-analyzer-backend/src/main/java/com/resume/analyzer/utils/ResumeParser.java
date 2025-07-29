package com.resume.analyzer.utils;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;

import java.io.InputStream;

public class ResumeParser {

    public static String extractText(InputStream inputStream) {
        try {
            AutoDetectParser parser = new AutoDetectParser();
            BodyContentHandler handler = new BodyContentHandler(-1); // -1 for unlimited length
            Metadata metadata = new Metadata();
            parser.parse(inputStream, handler, metadata);
            return handler.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
