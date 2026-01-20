package com.paperlesslab.batchworker.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class AccessEntryXml {

    @JacksonXmlProperty(isAttribute = true, localName = "documentId")
    public Long documentId;

    @JacksonXmlProperty(isAttribute = true, localName = "count")
    public long count;
}
