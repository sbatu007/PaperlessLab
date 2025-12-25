package com.paperlesslab.paperless.worker.genai;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OcrTextCleanerTest {

    @Test
    void removesPageMarkersAndFixesHyphenation() {
        String raw = """
                Page 1 of 3
                Digi-
                talisierung ist wichtig.
                2
                -----
                Page 2 of 3
                Normaler Text.
                """;

        String cleaned = OcrTextCleaner.clean(raw);

        assertThat(cleaned).doesNotContain("Page 1 of 3");
        assertThat(cleaned).doesNotContain("\n2\n");
        assertThat(cleaned).contains("Digitalisierung ist wichtig.");
        assertThat(cleaned).contains("Normaler Text.");
    }
}
