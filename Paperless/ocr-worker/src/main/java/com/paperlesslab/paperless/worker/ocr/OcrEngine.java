package com.paperlesslab.paperless.worker.ocr;

import java.nio.file.Path;

public interface OcrEngine {
    String extractText(Path pdfPath) throws Exception;
}
