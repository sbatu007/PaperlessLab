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

import java.time.Duration;
import java.util.List;

@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    private final WebClient webClient;
    private final String apiKey;
    private final String model;
    private final int maxOutputTokens;
    private final double temperature;

    public GeminiClient(
            WebClient.Builder builder,
            @Value("${GOOGLE_API_KEY:}") String apiKey,
            @Value("${GENAI_BASE_URL:https://generativelanguage.googleapis.com/v1beta}") String baseUrl,
            @Value("${GENAI_MODEL:gemini-2.0-flash-lite}") String model,
            @Value("${GENAI_MAX_OUTPUT_TOKENS:250}") int maxOutputTokens,
            @Value("${GENAI_TEMPERATURE:0.4}") double temperature
    ) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.apiKey = apiKey;
        this.model = model;
        this.maxOutputTokens = maxOutputTokens;
        this.temperature = temperature;
    }

    public String summarizeGermanBullets(String extractedText) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("GOOGLE_API_KEY is not set");
        }
        if (extractedText == null || extractedText.isBlank()) {
            return null;
        }

        String trimmed = extractedText.length() > 12000 ? extractedText.substring(0, 12000) : extractedText;

        String prompt = "Summarize the following OCR-extracted text in 5-8 bullet points. " +
                "Keep it concise and in German.\n\n" + trimmed;

        var req = GeminiRequest.defaultForPrompt(prompt, temperature, maxOutputTokens);

        var resp = webClient.post()
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
        return out.trim();
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
