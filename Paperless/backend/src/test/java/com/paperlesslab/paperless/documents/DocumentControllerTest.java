package com.paperlesslab.paperless.documents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.paperlesslab.paperless.dto.DocumentDto;
import com.paperlesslab.paperless.rabbitmq.RabbitMqProducer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DocumentControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;
    @MockBean
    RabbitMqProducer rabbitMqProducer;

    @Test
    void create_list_get_delete_exposesOnlyDtoFields() throws Exception {
        var dto = new DocumentDto(null, "abc.pdf", "hello");

        // CREATE -> 201 + Location, und beantwortetes JSON enthält KEIN 'uploadedAt' (Entity-Feld)
        var create = mvc.perform(post("/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/documents/\\d+")))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.filename").value("abc.pdf"))
                .andExpect(jsonPath("$.description").value("hello"))
                .andExpect(jsonPath("$.uploadedAt").doesNotExist())
                .andReturn();

        // LIST -> nur DTO-Felder
        mvc.perform(get("/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].filename").exists())
                .andExpect(jsonPath("$[0].description").exists())
                .andExpect(jsonPath("$[0].uploadedAt").doesNotExist());

        // GET by id -> nur DTO-Felder
        JsonNode saved = om.readTree(create.getResponse().getContentAsString());
        long id = saved.get("id").asLong();

        mvc.perform(get("/documents/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.filename").value("abc.pdf"))
                .andExpect(jsonPath("$.description").value("hello"))
                .andExpect(jsonPath("$.uploadedAt").doesNotExist());

        // DELETE -> 204
        mvc.perform(delete("/documents/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void uploadMultipart_and_noEntityFieldsExposed() throws Exception {
        var file = new MockMultipartFile("file", "hello.txt", "text/plain", "hi".getBytes());

        mvc.perform(multipart("/documents/upload")
                        .file(file)
                        .param("description", "from test"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filename").value("hello.txt"))
                .andExpect(jsonPath("$.description").value("from test"))
                .andExpect(jsonPath("$.uploadedAt").doesNotExist())
                .andExpect(header().string("Location", matchesPattern("/documents/\\d+")));
    }
}
