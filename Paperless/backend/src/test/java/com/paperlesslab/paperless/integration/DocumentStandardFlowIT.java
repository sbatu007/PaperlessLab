package com.paperlesslab.paperless.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paperlesslab.paperless.minio.FileStorageService;
import com.paperlesslab.paperless.rabbitmq.RabbitMqProducer;
import com.paperlesslab.paperless.service.DocumentSearchService;
import com.paperlesslab.paperless.service.SearchIndexService;
import org.junit.jupiter.api.Test;
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
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("it")
class DocumentStandardFlowIT {

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
        // Upload uses MinIO storage service -> mock it
        doNothing().when(fileStorageService).uploadPdf(any(), any());

        // 1) UPLOAD
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

        // 2) UPDATE description
        mockMvc.perform(put("/documents/{id}", docId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"description\":\"updated desc\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(docId))
                .andExpect(jsonPath("$.description").value("updated desc"));

        // 3) GET should return updated doc
        mockMvc.perform(get("/documents/{id}", docId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(docId))
                .andExpect(jsonPath("$.description").value("updated desc"));

        // 4) DELETE
        doNothing().when(fileStorageService).deletePdf(any());
        mockMvc.perform(delete("/documents/{id}", docId))
                .andExpect(status().isNoContent());

        // 5) GET after delete -> 404
        mockMvc.perform(get("/documents/{id}", docId))
                .andExpect(status().isNotFound());
    }
}
