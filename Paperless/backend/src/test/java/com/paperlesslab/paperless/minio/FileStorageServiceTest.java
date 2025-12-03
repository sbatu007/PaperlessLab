package com.paperlesslab.paperless.minio;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FileStorageServiceTest {

    @Test
    void save_createsBucketIfNotExists_andStoresFile() throws Exception {
        MinioClient client = mock(MinioClient.class);
        when(client.bucketExists(any(BucketExistsArgs.class))).thenReturn(false);

        FileStorageService service = new FileStorageService(client, "documents");

        assertDoesNotThrow(() ->
                service.save("test.pdf",
                        new ByteArrayInputStream("hello".getBytes()),
                        5L,
                        "application/pdf")
        );

        verify(client).bucketExists(any(BucketExistsArgs.class));
        verify(client).makeBucket(any(MakeBucketArgs.class));
        verify(client).putObject(any(PutObjectArgs.class));
    }

    @Test
    void save_doesNotCreateBucketIfBucketExists() throws Exception {
        MinioClient client = mock(MinioClient.class);
        when(client.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        FileStorageService service = new FileStorageService(client, "documents");

        service.save("test.pdf",
                new ByteArrayInputStream("hello".getBytes()),
                5L,
                "application/pdf");

        verify(client).bucketExists(any(BucketExistsArgs.class));
        verify(client, never()).makeBucket(any(MakeBucketArgs.class));
        verify(client).putObject(any(PutObjectArgs.class));
    }
}