package com.paperlesslab.paperless.worker.genai;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.*;

class GeminiClientTest {

    private MockWebServer server;

    @BeforeEach
    void setUp() throws Exception {
        server = new MockWebServer();
        server.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    void parsesFirstCandidateText() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"candidates\":[{\"content\":{\"parts\":[{\"text\":\"Hello Result\"}]}}]}"));

        WebClient.Builder builder = WebClient.builder();
        GeminiClient client = new GeminiClient(
                builder,
                "test-key",
                server.url("/v1beta").toString(),
                "gemini-2.5-flash-lite",
                50,
                0.1
        );

        String out = client.summarizeGermanBullets("some text");
        assertEquals("Hello Result", out);
    }

    @Test
    void missingApiKeyThrows() {
        WebClient.Builder builder = WebClient.builder();
        GeminiClient client = new GeminiClient(
                builder,
                "",
                server.url("/v1beta").toString(),
                "gemini-2.5-flash-lite",
                50,
                0.1
        );

        assertThrows(IllegalStateException.class, () -> client.summarizeGermanBullets("x"));
    }

    @Test
    void emptyTextReturnsNull() {
        WebClient.Builder builder = WebClient.builder();
        GeminiClient client = new GeminiClient(
                builder,
                "test-key",
                server.url("/v1beta").toString(),
                "gemini-2.5-flash-lite",
                50,
                0.1
        );

        assertNull(client.summarizeGermanBullets(""));
        assertNull(client.summarizeGermanBullets(null));
    }

    @Test
    void handlesEmptyResponse() {
        server.enqueue(new MockResponse()
                .setHeader("Content-Type", "application/json")
                .setBody("{\"candidates\":[]}"));

        WebClient.Builder builder = WebClient.builder();
        GeminiClient client = new GeminiClient(
                builder,
                "test-key",
                server.url("/v1beta").toString(),
                "gemini-2.5-flash-lite",
                50,
                0.1
        );

        assertNull(client.summarizeGermanBullets("some text"));
    }
}
