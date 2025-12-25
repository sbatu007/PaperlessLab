package com.paperlesslab.paperless.worker.genai;

import java.util.*;
import java.util.regex.Pattern;

public final class OcrTextCleaner {

    private OcrTextCleaner() {}

    private static final Pattern PAGE_NUMBER_ONLY = Pattern.compile("^\\d{1,4}$");
    private static final Pattern PAGE_X_OF_Y = Pattern.compile("^(page\\s+)?\\d{1,4}\\s*(/|of|von)\\s*\\d{1,4}$",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern DATE_LIKE = Pattern.compile("^(\\d{1,2}[./-]\\d{1,2}[./-]\\d{2,4}|\\d{4}-\\d{2}-\\d{2}).*$");
    private static final Pattern MOSTLY_NON_LETTERS = Pattern.compile("^[^\\p{L}]{0,3}[^\\p{L}\\p{N}]+[^\\p{L}]{0,3}$");


    public static String clean(String raw) {
        if (raw == null) return null;

        String s = raw
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[\\p{Cntrl}&&[^\\n\\t]]", " ");

        s = s.replaceAll("-\\s*\\n\\s*", "");

        String[] lines = s.split("\\n");
        List<String> trimmedLines = new ArrayList<>(lines.length);
        for (String line : lines) {
            String t = line.trim();
            if (!t.isEmpty()) trimmedLines.add(t);
        }

        if (trimmedLines.isEmpty()) return "";

        Map<String, Integer> freq = new HashMap<>();
        for (String line : trimmedLines) {
            String key = normalizeForFrequency(line);
            if (key.length() <= 80) {
                freq.put(key, freq.getOrDefault(key, 0) + 1);
            }
        }

        int repeatThreshold = Math.max(3, trimmedLines.size() / 30); // heuristic
        Set<String> repeated = new HashSet<>();
        for (var e : freq.entrySet()) {
            if (e.getValue() >= repeatThreshold) repeated.add(e.getKey());
        }

        List<String> kept = new ArrayList<>(trimmedLines.size());
        for (String line : trimmedLines) {
            String norm = normalizeForFrequency(line);

            if (repeated.contains(norm)) continue;

            if (PAGE_NUMBER_ONLY.matcher(line).matches()) continue;
            if (PAGE_X_OF_Y.matcher(line.replaceAll("\\s+", " ").trim()).matches()) continue;

            if (line.length() <= 2) continue;

            if (MOSTLY_NON_LETTERS.matcher(line).matches()) continue;

            if (DATE_LIKE.matcher(line).matches() && line.length() < 25) continue;

            if (isLikelyNoise(line)) continue;

            kept.add(line);
        }

        String joined = String.join("\n", kept);

        joined = joined.replaceAll("\\n{3,}", "\n\n");

        joined = joined.replaceAll("[ \\t]{2,}", " ");

        return joined.trim();
    }

    private static String normalizeForFrequency(String line) {
        return line.toLowerCase(Locale.ROOT)
                .replaceAll("\\d", "#")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private static boolean isLikelyNoise(String line) {
        int letters = 0;
        int digits = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (Character.isLetter(c)) letters++;
            else if (Character.isDigit(c)) digits++;
        }

        int len = line.length();
        if (len <= 10) {
            return letters <= 1 && digits <= 2;
        }

        double letterRatio = (double) letters / (double) len;

        return len < 30 && letterRatio < 0.25;
    }
}
