package com.paperlesslab.paperless.documents;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.paperlesslab.paperless.documents.DocumentDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class DocumentControllerTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @Test
    void createListGetDelete() throws Exception {
        var dto = new DocumentDto(null, "abc.pdf", "hello");


// create -> 201 + Location
        var create = mvc.perform(post("/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.matchesPattern("/documents/\\d+")))
                .andExpect(jsonPath("$.filename").value("abc.pdf"))
                .andReturn();


        mvc.perform(get("/documents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].filename").exists());


// GET by ID
        var body = create.getResponse().getContentAsString();
        var saved = om.readTree(body);
        long id = saved.get("id").asLong();


        mvc.perform(get("/documents/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));


// DELETE
        mvc.perform(delete("/documents/" + id))
                .andExpect(status().isNoContent());
    }


    @Test
    void uploadMultipart() throws Exception {
        var file = new MockMultipartFile("file", "hello.txt", "text/plain", "hi".getBytes());
        mvc.perform(multipart("/documents/upload").file(file)
                        .param("description", "from test"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.filename").value("hello.txt"));
    }
}
