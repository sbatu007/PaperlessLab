package com.paperlesslab.paperless.worker.ocr;

import com.paperlesslab.paperless.rabbitmq.DocumentUploadMessage;
import com.paperlesslab.paperless.worker.genai.GeminiClient;
import com.paperlesslab.paperless.worker.rabbitmq.IndexProducer;
import com.paperlesslab.paperless.worker.rabbitmq.ResultProducer;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OcrServicePublishTest {

    @Test
    void publishesIndexMessageAfterOcr() throws Exception {
        MinioClient minio = mock(MinioClient.class);

        GetObjectResponse response = mock(GetObjectResponse.class);
        // ensure stream ends immediately
        when(response.read(any(byte[].class), anyInt(), anyInt())).thenReturn(-1);
        when(response.read()).thenReturn(-1);

        when(minio.getObject(any(GetObjectArgs.class))).thenReturn(response);

        OcrEngine ocrEngine = mock(OcrEngine.class);
        when(ocrEngine.extractText(any())).thenReturn("Hello OCR Text");

        GeminiClient gemini = mock(GeminiClient.class);
        when(gemini.summarizeGermanBullets(anyString())).thenReturn("• Summary");

        ResultProducer resultProducer = mock(ResultProducer.class);
        IndexProducer indexProducer = mock(IndexProducer.class);

        OcrService svc = new OcrService(
                minio,
                "documents",
                ocrEngine,
                gemini,
                resultProducer,
                indexProducer
        );

        svc.process(new DocumentUploadMessage(1L, "HelloWorld.pdf", "desc"));

        // verify index publish
        var captor = org.mockito.ArgumentCaptor.forClass(com.paperlesslab.paperless.rabbitmq.IndexMessage.class);
        verify(indexProducer, times(1)).send(captor.capture());

        var msg = captor.getValue();
        assertEquals(1L, msg.documentId());
        assertEquals("HelloWorld.pdf", msg.filename());
        assertTrue(msg.ocrText().contains("Hello"));
    }
}
