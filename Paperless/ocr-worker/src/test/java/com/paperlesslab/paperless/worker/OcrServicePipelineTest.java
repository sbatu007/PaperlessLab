package com.paperlesslab.paperless.worker;

import com.paperlesslab.paperless.rabbitmq.DocumentUploadMessage;
import com.paperlesslab.paperless.rabbitmq.GenAiResultMessage;
import com.paperlesslab.paperless.worker.genai.GeminiClient;
import com.paperlesslab.paperless.worker.ocr.OcrEngine;
import com.paperlesslab.paperless.worker.ocr.OcrService;
import com.paperlesslab.paperless.worker.rabbitmq.ResultProducer;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class OcrServicePipelineTest {

    @Test
    void afterOcrItPublishesSummaryMessage() throws Exception {
        MinioClient minio = mock(MinioClient.class);
        OcrEngine engine = mock(OcrEngine.class);
        GeminiClient gemini = mock(GeminiClient.class);
        ResultProducer producer = mock(ResultProducer.class);

        GetObjectResponse response = mock(GetObjectResponse.class);
        when(response.read(any(byte[].class))).thenReturn(-1);
        when(response.read(any(byte[].class), anyInt(), anyInt())).thenReturn(-1);

        when(minio.getObject(any(GetObjectArgs.class))).thenReturn(response);
        when(engine.extractText(any())).thenReturn("ocr text");
        when(gemini.summarizeGermanBullets("ocr text")).thenReturn("result");

        OcrService service = new OcrService(minio, "documents", engine, gemini, producer);

        service.process(new DocumentUploadMessage(1L, "test.pdf", "desc"));

        ArgumentCaptor<GenAiResultMessage> captor = ArgumentCaptor.forClass(GenAiResultMessage.class);
        verify(producer, times(1)).send(captor.capture());
        verify(gemini, times(1)).summarizeGermanBullets("ocr text");

        GenAiResultMessage sent = captor.getValue();
        assertEquals(1L, sent.documentId());
        assertEquals("result", sent.result());
        assertEquals("ocr text", sent.ocrText());
    }

    @Test
    void geminiFailureStillPublishes() throws Exception {
        MinioClient minio = mock(MinioClient.class);
        OcrEngine engine = mock(OcrEngine.class);
        GeminiClient gemini = mock(GeminiClient.class);
        ResultProducer producer = mock(ResultProducer.class);

        GetObjectResponse response = mock(GetObjectResponse.class);
        when(response.read(any(byte[].class))).thenReturn(-1);
        when(response.read(any(byte[].class), anyInt(), anyInt())).thenReturn(-1);

        when(minio.getObject(any(GetObjectArgs.class))).thenReturn(response);
        when(engine.extractText(any())).thenReturn("ocr text");
        when(gemini.summarizeGermanBullets("ocr text")).thenThrow(new RuntimeException("API Error"));

        OcrService service = new OcrService(minio, "documents", engine, gemini, producer);

        service.process(new DocumentUploadMessage(1L, "test.pdf", "desc"));

        ArgumentCaptor<GenAiResultMessage> captor = ArgumentCaptor.forClass(GenAiResultMessage.class);
        verify(producer, times(1)).send(captor.capture());

        GenAiResultMessage sent = captor.getValue();
        assertEquals(1L, sent.documentId());
        assertNull(sent.result());
        assertEquals("ocr text", sent.ocrText());
    }
}
