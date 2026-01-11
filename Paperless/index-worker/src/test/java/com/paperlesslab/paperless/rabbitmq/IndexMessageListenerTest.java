package com.paperlesslab.paperless.rabbitmq;

import com.paperlesslab.paperless.indexworker.elasticsearch.IndexService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

class IndexMessageListenerTest {

    @Test
    void listenerCallsIndexService() {
        IndexService indexService = mock(IndexService.class);
        IndexMessageListener listener = new IndexMessageListener(indexService);

        listener.handle(new IndexMessage(1L, "HelloWorld.pdf", "Hello", "Summary"));

        verify(indexService).index(1L, "HelloWorld.pdf", "Hello", "Summary");
    }
}
