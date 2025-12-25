package com.paperlesslab.paperless.worker.genai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    // small generic helpers to remove obvious OCR noise (no external class needed)
    private static final Pattern PAGE_NUMBER_ONLY = Pattern.compile("^\\d{1,4}$");
    private static final Pattern PAGE_X_OF_Y = Pattern.compile("^(page\\s+)?\\d{1,4}\\s*(/|of|von)\\s*\\d{1,4}$",
            Pattern.CASE_INSENSITIVE);

    private final WebClient webClient;
    private final String apiKey;
    private final String model;
    private final int maxOutputTokens;
    private final double temperature;
    private final boolean logRequest;
    private final boolean logResponse;

    public GeminiClient(
            WebClient.Builder builder,
            @Value("${GOOGLE_API_KEY:}") String apiKey,
            @Value("${GENAI_BASE_URL:https://generativelanguage.googleapis.com/v1beta}") String baseUrl,
            @Value("${GENAI_MODEL:gemini-2.5-flash-lite}") String model,
            @Value("${GENAI_MAX_OUTPUT_TOKENS:250}") int maxOutputTokens,
            @Value("${GENAI_TEMPERATURE:0.4}") double temperature,
            @Value("${GENAI_LOG_REQUEST:false}") boolean logRequest,
            @Value("${GENAI_LOG_RESPONSE:false}") boolean logResponse
    ) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.model = model;
        this.maxOutputTokens = maxOutputTokens;
        this.temperature = temperature;
        this.logRequest = logRequest;
        this.logResponse = logResponse;
    }

    public String summarizeGermanBullets(String extractedText) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GOOGLE_API_KEY is not set");
        }
        if (extractedText == null || extractedText.isBlank()) {
            return null;
        }

        // 1) clean OCR text inline (generic heuristics)
        String cleaned = cleanOcrText(extractedText);
        if (cleaned.isBlank()) {
            return null;
        }

        // 2) size limit AFTER cleaning
        String trimmed = cleaned.length() > 12000 ? cleaned.substring(0, 12000) : cleaned;

        // 3) much stronger general prompt
        String prompt = """
Du bist ein professioneller Assistent für Zusammenfassungen.

Regeln:
- Verwende ausschließlich Informationen aus dem Text. Erfinde nichts.
- Ignoriere OCR-Artefakte (Seitenzahlen, Kopf-/Fußzeilen, Layoutreste, einzelne Buchstaben, kaputte Wörter).
- Keine Zitate und keine wörtlichen Passagen.

Aufgabe:
- Erstelle eine Zusammenfassung als Stichpunkte auf Deutsch.
- Ausgabe muss aus genau 6 bis 8 Zeilen bestehen.
- Jede Zeile muss mit "- " beginnen.
- Keine Überschrift, kein Fließtext, keine Nummerierung, keine leeren Zeilen.

OCR-Text:
""" + "\n" + trimmed;

        if (logRequest) {
            log.info("Gemini request config: model={}, maxTokens={}, temperature={}", model, maxOutputTokens, temperature);
            log.info("Gemini prompt len={} preview={}", prompt.length(), preview(prompt, 200));
        }

        GeminiRequest req = GeminiRequest.defaultForPrompt(prompt, temperature, maxOutputTokens);

        try {
            GeminiResponse resp = webClient.post()
                    .uri("/models/%s:generateContent".formatted(model))
                    .header("X-goog-api-key", apiKey)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(req)
                    .retrieve()
                    .bodyToMono(GeminiResponse.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            String out = resp == null ? null : resp.firstText();
            if (out == null || out.isBlank()) {
                log.warn("Gemini returned empty output");
                return null;
            }

            if (logResponse) {
                log.info("Gemini response len={} preview={}", out.length(), preview(out, 300));
            }

            return out.trim();
        } catch (WebClientResponseException e) {
            log.error("Gemini API error: status={} body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("Gemini call failed (unexpected)", e);
            throw e;
        }
    }

    private static String cleanOcrText(String raw) {
        String s = raw
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceAll("[\\p{Cntrl}&&[^\\n\\t]]", " ");

        // "Transfor-\nmation" -> "Transformation"
        s = s.replaceAll("-\\s*\\n\\s*", "");

        String[] lines = s.split("\\n");
        StringBuilder out = new StringBuilder(s.length());

        for (String line : lines) {
            String t = line.trim();
            if (t.isEmpty()) continue;

            // drop ultra-short junk ("RI", "| |", etc.)
            if (t.length() <= 2) continue;

            // drop page numbers / page markers
            if (PAGE_NUMBER_ONLY.matcher(t).matches()) continue;
            String compressed = t.replaceAll("\\s+", " ");
            if (PAGE_X_OF_Y.matcher(compressed).matches()) continue;

            // drop separator-like lines
            if (isMostlyPunctuation(t)) continue;

            out.append(t).append('\n');
        }

        return out.toString()
                .replaceAll("\\n{3,}", "\n\n")
                .replaceAll("[ \\t]{2,}", " ")
                .trim();
    }

    private static boolean isMostlyPunctuation(String line) {
        int lettersOrDigits = 0;
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (Character.isLetterOrDigit(c)) lettersOrDigits++;
        }
        // if very few letters/digits, likely just decoration/separator
        return line.length() < 40 && lettersOrDigits <= 2;
    }

    private static String preview(String s, int max) {
        if (s == null) return "null";
        String oneLine = s.replace("\n", "\\n");
        return oneLine.length() <= max ? oneLine : oneLine.substring(0, max) + "...";
    }

    public record GeminiRequest(
            @JsonProperty("generationConfig") GenerationConfig generationConfig,
            @JsonProperty("contents") List<Content> contents
    ) {
        public static GeminiRequest defaultForPrompt(String prompt, double temperature, int maxOutputTokens) {
            return new GeminiRequest(
                    new GenerationConfig(temperature, 40, 0.8, maxOutputTokens),
                    List.of(new Content(List.of(new Part(prompt))))
            );
        }
    }

    public record GenerationConfig(
            @JsonProperty("temperature") double temperature,
            @JsonProperty("topK") int topK,
            @JsonProperty("topP") double topP,
            @JsonProperty("maxOutputTokens") int maxOutputTokens
    ) {}

    public record Content(@JsonProperty("parts") List<Part> parts) {}
    public record Part(@JsonProperty("text") String text) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GeminiResponse(@JsonProperty("candidates") List<Candidate> candidates) {
        public String firstText() {
            if (candidates == null || candidates.isEmpty()) return null;
            Candidate c = candidates.get(0);
            if (c == null || c.content == null || c.content.parts == null || c.content.parts.isEmpty()) return null;
            PartOut p = c.content.parts.get(0);
            return p == null ? null : p.text;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Candidate(@JsonProperty("content") ContentOut content) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record ContentOut(@JsonProperty("parts") List<PartOut> parts) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PartOut(@JsonProperty("text") String text) {}
}
