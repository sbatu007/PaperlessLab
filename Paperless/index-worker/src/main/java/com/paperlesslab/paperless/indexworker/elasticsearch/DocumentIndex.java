package com.paperlesslab.paperless.indexworker.elasticsearch;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "documents")
public class DocumentIndex {

    @Id
    private String id;

    @Field(type = FieldType.Long)
    private Long documentId;

    @Field(type = FieldType.Keyword)
    private String filename;

    @Field(type = FieldType.Text)
    private String ocrText;

    @Field(type = FieldType.Text)
    private String result;

    @Field(type = FieldType.Date)
    private Instant indexedAt;
}
