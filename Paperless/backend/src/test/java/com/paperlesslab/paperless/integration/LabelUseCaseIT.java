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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Sprint 7 - Unique feature integration test:
 * Upload document -> create labels -> tag document -> remove labels -> delete label
 * REST level, real PostgreSQL via Testcontainers, infra mocked.
 *
 * Includes per-step performance logging.
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("it")
class LabelUseCaseIT {

    private static final Logger log = LoggerFactory.getLogger(LabelUseCaseIT.class);

    @Container
    @SuppressWarnings("resource") // IntelliJ warns about try-with-resources; fine for @Container lifecycle
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
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

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // Only here to override Spring's internal rabbit listener registry bean (prevents context startup issues)
    @MockBean(name = "org.springframework.amqp.rabbit.config.internalRabbitListenerEndpointRegistry")
    private RabbitListenerEndpointRegistry rabbitListenerEndpointRegistry;

    // Infra mocks: keep IT scope REST + DB only
    @MockBean private FileStorageService fileStorageService;
    @MockBean private RabbitMqProducer rabbitMqProducer;
    @MockBean private SearchIndexService searchIndexService;
    @MockBean private DocumentSearchService documentSearchService;

    @Test
    void labels_flow_upload_tag_remove_deleteLabel() throws Exception {
        long t0 = System.currentTimeMillis();

        doNothing().when(fileStorageService).uploadPdf(any(), any());
        doNothing().when(fileStorageService).deletePdf(any());

        // ---------------------------------------------------------------------
        // STEP 1: upload document first (required order)
        // ---------------------------------------------------------------------
        log.info("[STEP 1] upload document");
        long s1 = System.currentTimeMillis();

        MockMultipartFile pdf = new MockMultipartFile(
                "file", "LabelDoc.pdf", "application/pdf", "%PDF-1.4 fake".getBytes()
        );

        String uploadJson = mockMvc.perform(multipart("/documents/upload")
                        .file(pdf)
                        .param("description", "doc for label usecase"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.labels").isArray())
                .andExpect(jsonPath("$.labels", hasSize(0)))
                .andReturn()
                .getResponse()
                .getContentAsString();

        long docId = objectMapper.readTree(uploadJson).get("id").asLong();
        log.info("[PERF] upload took {} ms", (System.currentTimeMillis() - s1));

        // ---------------------------------------------------------------------
        // STEP 2: create two labels
        // ---------------------------------------------------------------------
        log.info("[STEP 2] create labels");
        long s2 = System.currentTimeMillis();

        long labelAId = createLabel("Finance");
        long labelBId = createLabel("2026");

        log.info("[PERF] create labels took {} ms", (System.currentTimeMillis() - s2));

        // ---------------------------------------------------------------------
        // STEP 3: tag document with those labels
        // ---------------------------------------------------------------------
        log.info("[STEP 3] tag document");
        long s3 = System.currentTimeMillis();

        mockMvc.perform(put("/documents/{id}/labels", docId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"labelIds\":[" + labelAId + " , " + labelBId + "]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(docId))
                .andExpect(jsonPath("$.labels").isArray())
                .andExpect(jsonPath("$.labels", hasSize(2)));

        log.info("[PERF] tag document took {} ms", (System.currentTimeMillis() - s3));

        // Verify GET returns labels
        log.info("[STEP 3b] verify tags via GET");
        mockMvc.perform(get("/documents/{id}", docId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels", hasSize(2)));

        // ---------------------------------------------------------------------
        // STEP 4: remove labels by setting empty list
        // ---------------------------------------------------------------------
        log.info("[STEP 4] remove labels");
        long s4 = System.currentTimeMillis();

        mockMvc.perform(put("/documents/{id}/labels", docId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"labelIds\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.labels", hasSize(0)));

        log.info("[PERF] remove labels took {} ms", (System.currentTimeMillis() - s4));

        // ---------------------------------------------------------------------
        // STEP 5: delete a label
        // ---------------------------------------------------------------------
        log.info("[STEP 5] delete label");
        long s5 = System.currentTimeMillis();

        mockMvc.perform(delete("/labels/{id}", labelAId))
                .andExpect(status().isNoContent());

        log.info("[PERF] delete label took {} ms", (System.currentTimeMillis() - s5));

        // Sanity: labels endpoint still works
        mockMvc.perform(get("/labels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        log.info("[PERF] total test took {} ms", (System.currentTimeMillis() - t0));
    }

    private long createLabel(String name) throws Exception {
        String json = mockMvc.perform(post("/labels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"" + name + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value(name))
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode n = objectMapper.readTree(json);
        return n.get("id").asLong();
    }
}
