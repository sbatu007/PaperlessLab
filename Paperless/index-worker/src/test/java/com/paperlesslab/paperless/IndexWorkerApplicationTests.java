package com.paperlesslab.paperless;

import com.paperlesslab.paperless.indexworker.elasticsearch.DocumentIndexRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class IndexWorkerApplicationTests {


    @MockBean
    private DocumentIndexRepository documentIndexRepository;

    @Test
    void contextLoads() {
    }
}
