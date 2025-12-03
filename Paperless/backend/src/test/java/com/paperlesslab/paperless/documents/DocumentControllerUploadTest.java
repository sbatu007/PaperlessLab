package com.paperlesslab.paperless.documents;

import com.paperlesslab.paperless.controller.DocumentController;
import com.paperlesslab.paperless.entity.Document;
import com.paperlesslab.paperless.minio.FileStorageService;
import com.paperlesslab.paperless.service.DocumentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
class DocumentControllerUploadTest {

    @Autowired
    MockMvc mvc;

    @MockBean
    DocumentService documentService;

    @MockBean
    FileStorageService fileStorageService;

    @Test
    void uploadMultipart_storesFileAndDoesNotExposeEntityFields() throws Exception {
        var file = new MockMultipartFile(
                "file",
                "hello.pdf",
                "application/pdf",
                "dummy content".getBytes()
        );

        // File in MinIO speichern simulieren
        doNothing().when(fileStorageService)
                .uploadPdf(any(), anyString());

        // DocumentService simulieren
        Document saved = new Document();
        saved.setId(1L);
        saved.setFilename("hello.pdf");
        saved.setDescription("from test");

        when(documentService.create(any(Document.class))).thenReturn(saved);

        mvc.perform(multipart("/documents/upload")
                        .file(file)
                        .param("description", "from test")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/documents/\\d+")))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.filename").value("hello.pdf"))
                .andExpect(jsonPath("$.description").value("from test"))
                .andExpect(jsonPath("$.uploadedAt").doesNotExist());
    }
}
