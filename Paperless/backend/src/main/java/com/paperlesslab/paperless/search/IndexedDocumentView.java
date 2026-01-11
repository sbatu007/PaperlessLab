package com.paperlesslab.paperless.search;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Document(indexName = "documents")
public class IndexedDocumentView {

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
}
