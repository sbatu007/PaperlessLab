package com.paperlesslab.paperless.worker;

import com.paperlesslab.paperless.rabbitmq.DocumentUploadMessage;
import com.paperlesslab.paperless.worker.genai.GeminiClient;
import com.paperlesslab.paperless.worker.ocr.OcrEngine;
import com.paperlesslab.paperless.worker.ocr.OcrService;
import com.paperlesslab.paperless.worker.rabbitmq.ResultProducer;
import io.minio.GetObjectArgs;
import io.minio.GetObjectResponse;
import io.minio.MinioClient;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

class OcrServiceTest {

    @Test
    void process_downloadsFromMinio_andRunsOcr() throws Exception {
        MinioClient minioClient = mock(MinioClient.class);
        OcrEngine ocrEngine = mock(OcrEngine.class);
        GeminiClient geminiClient = mock(GeminiClient.class);
        ResultProducer resultProducer = mock(ResultProducer.class);

        GetObjectResponse response = mock(GetObjectResponse.class);
        when(response.read(any(byte[].class))).thenReturn(-1);
        when(response.read(any(byte[].class), anyInt(), anyInt())).thenReturn(-1);

        when(minioClient.getObject(any(GetObjectArgs.class)))
                .thenReturn(response);

        when(ocrEngine.extractText(any()))
                .thenReturn("dummy text");

        when(geminiClient.summarizeGermanBullets(any()))
                .thenReturn("• Zusammenfassung");

        OcrService service = new OcrService(
                minioClient,
                "documents",
                ocrEngine,
                geminiClient,
                resultProducer
        );

        DocumentUploadMessage message = new DocumentUploadMessage(
                1L,
                "test.pdf",
                "demo"
        );

        assertDoesNotThrow(() -> service.process(message));

        verify(minioClient, times(1)).getObject(any(GetObjectArgs.class));
        verify(ocrEngine, times(1)).extractText(any());
        verify(geminiClient, times(1)).summarizeGermanBullets(any());
        verify(resultProducer, times(1)).send(any());
    }
}
