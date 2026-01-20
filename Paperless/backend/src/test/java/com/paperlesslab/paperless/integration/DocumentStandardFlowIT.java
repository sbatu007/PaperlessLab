package com.paperlesslab.paperless.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paperlesslab.paperless.minio.FileStorageService;
import com.paperlesslab.paperless.rabbitmq.RabbitMqProducer;
import com.paperlesslab.paperless.service.DocumentSearchService;
import com.paperlesslab.paperless.service.SearchIndexService;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Sprint 7 - Standard integration test:
 * Upload -> Update -> Delete (REST level, DB real, infra mocked).
 *
 * Includes performance tracking (per-step timestamps) as required in Sprint 7.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("it")
class DocumentStandardFlowIT {

    private static final Logger log = LoggerFactory.getLogger(DocumentStandardFlowIT.class);

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("paperless")
            .withUsername("paperless_user")
            .withPassword("secret");

    @DynamicPropertySource
    static void dbProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    // Safety: if any rabbit infrastructure is still present, registry bean type must match.
    @MockBean(name = "org.springframework.amqp.rabbit.config.internalRabbitListenerEndpointRegistry")
    RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    // Infra mocks so IT is REST+DB only
    @MockBean FileStorageService fileStorageService;
    @MockBean RabbitMqProducer rabbitMqProducer;
    @MockBean SearchIndexService searchIndexService;
    @MockBean DocumentSearchService documentSearchService;

    @Test
    void upload_update_delete() throws Exception {
        long t0 = System.currentTimeMillis();

        // Upload uses MinIO storage service -> mock it
        doNothing().when(fileStorageService).uploadPdf(any(), any());
        doNothing().when(fileStorageService).deletePdf(any());

        // ---------------------------------------------------------------------
        // STEP 1: UPLOAD
        // ---------------------------------------------------------------------
        long s1 = System.currentTimeMillis();
        log.info("[STEP 1] Upload document");

        MockMultipartFile pdf = new MockMultipartFile(
                "file", "HelloWorld.pdf", "application/pdf", "%PDF-1.4 fake".getBytes()
        );

        String uploadJson = mockMvc.perform(multipart("/documents/upload")
                        .file(pdf)
                        .param("description", "initial desc"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.filename").exists())
                .andReturn().getResponse().getContentAsString();

        JsonNode uploaded = objectMapper.readTree(uploadJson);
        long docId = uploaded.get("id").asLong();

        log.info("[PERF] upload took {} ms", (System.currentTimeMillis() - s1));

        // ---------------------------------------------------------------------
        // STEP 2: UPDATE description
        // ---------------------------------------------------------------------
        long s2 = System.currentTimeMillis();
        log.info("[STEP 2] Update document description");

        mockMvc.perform(put("/documents/{id}", docId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"updated desc\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(docId))
                .andExpect(jsonPath("$.description").value("updated desc"));

        log.info("[PERF] update took {} ms", (System.currentTimeMillis() - s2));

        // ---------------------------------------------------------------------
        // STEP 3: GET should return updated doc
        // ---------------------------------------------------------------------
        long s3 = System.currentTimeMillis();
        log.info("[STEP 3] Get document and verify update");

        mockMvc.perform(get("/documents/{id}", docId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(docId))
                .andExpect(jsonPath("$.description").value("updated desc"));

        log.info("[PERF] get took {} ms", (System.currentTimeMillis() - s3));

        // ---------------------------------------------------------------------
        // STEP 4: DELETE
        // ---------------------------------------------------------------------
        long s4 = System.currentTimeMillis();
        log.info("[STEP 4] Delete document");

        mockMvc.perform(delete("/documents/{id}", docId))
                .andExpect(status().isNoContent());

        log.info("[PERF] delete took {} ms", (System.currentTimeMillis() - s4));

        // ---------------------------------------------------------------------
        // STEP 5: GET after delete -> 404
        // ---------------------------------------------------------------------
        long s5 = System.currentTimeMillis();
        log.info("[STEP 5] Verify document is gone (404)");

        mockMvc.perform(get("/documents/{id}", docId))
                .andExpect(status().isNotFound());

        log.info("[PERF] get-after-delete took {} ms", (System.currentTimeMillis() - s5));

        // ---------------------------------------------------------------------
        // TOTAL
        // ---------------------------------------------------------------------
        log.info("[PERF] total test took {} ms", (System.currentTimeMillis() - t0));
    }
}
