package com.paperlesslab.batchworker.xml;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public class AccessLogsXml {

    @JacksonXmlProperty(isAttribute = true, localName = "day")
    public String day;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "access")
    public List<AccessEntryXml> access;
}
