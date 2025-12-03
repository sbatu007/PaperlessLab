package com.paperlesslab.paperless.worker.ocr;

import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class TesseractOcrEngine implements OcrEngine {

    private static final Logger log = LoggerFactory.getLogger(TesseractOcrEngine.class);

    private final Tesseract tesseract;

    public TesseractOcrEngine() {
        this.tesseract = new Tesseract();
        tesseract.setDatapath("/usr/share/tesseract-ocr/5/tessdata");
        tesseract.setLanguage("eng");
    }

    @Override
    public String extractText(Path pdfPath) throws TesseractException {
        log.info("Running OCR on file {}", pdfPath);
        return tesseract.doOCR(pdfPath.toFile());
    }
}
