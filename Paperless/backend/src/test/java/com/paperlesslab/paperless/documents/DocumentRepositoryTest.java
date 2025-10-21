package com.paperlesslab.paperless.documents;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;


import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class DocumentRepositoryTest {
    @Autowired DocumentRepository repo;


    @Test
    void savesAndFinds() {
        var doc = new Document();
        doc.setFilename("test.pdf");
        var saved = repo.save(doc);
        assertThat(saved.getId()).isNotNull();
        assertThat(repo.findById(saved.getId())).isPresent();
    }
}
