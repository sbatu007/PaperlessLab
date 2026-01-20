package com.paperlesslab.batchworker.job;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.paperlesslab.batchworker.entity.DocumentAccessStat;
import com.paperlesslab.batchworker.repo.DocumentAccessStatRepository;
import com.paperlesslab.batchworker.xml.AccessLogsXml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.*;
import java.time.LocalDate;

@Component
public class AccessLogBatchJob {

    private static final Logger log = LoggerFactory.getLogger(AccessLogBatchJob.class);

    private final DocumentAccessStatRepository repo;
    private final XmlMapper xmlMapper = new XmlMapper();

    @Value("${batch.input-dir:./batch-input}")
    private String inputDir;

    @Value("${batch.archive-dir:./batch-archive}")
    private String archiveDir;

    @Value("${batch.pattern:*.xml}")
    private String pattern;

    public AccessLogBatchJob(DocumentAccessStatRepository repo) {
        this.repo = repo;
    }

    // every minute (configurable)
    @Scheduled(
            fixedDelayString = "${batch.fixed-delay-ms:60000}",
            initialDelayString = "${batch.initial-delay-ms:5000}"
    )
    public void scheduledRun() {
        runOnce();
    }

    @Transactional
    public void runOnce() {
        long t0 = System.currentTimeMillis();
        try {
            Path in = Paths.get(inputDir);
            Path arch = Paths.get(archiveDir);
            Files.createDirectories(in);
            Files.createDirectories(arch);

            int processedFiles = 0;

            try (DirectoryStream<Path> stream = Files.newDirectoryStream(in, pattern)) {
                for (Path file : stream) {
                    processOne(file, arch);
                    processedFiles++;
                }
            }

            log.info("[BATCH][PERF] runOnce processed {} file(s) in {} ms",
                    processedFiles, (System.currentTimeMillis() - t0));
        } catch (Exception e) {
            log.error("[BATCH] runOnce failed", e);
        }
    }

    private void processOne(Path file, Path archiveDir) throws Exception {
        long tFile = System.currentTimeMillis();
        log.info("[BATCH] Processing {}", file.getFileName());

        AccessLogsXml logs = xmlMapper.readValue(file.toFile(), AccessLogsXml.class);
        if (logs.day == null || logs.day.isBlank()) {
            throw new IllegalArgumentException("XML missing day attribute: " + file.getFileName());
        }

        LocalDate day = LocalDate.parse(logs.day);

        if (logs.access != null) {
            for (var entry : logs.access) {
                if (entry.documentId == null) continue;

                repo.findByDocumentIdAndDay(entry.documentId, day)
                        .ifPresentOrElse(existing -> {
                            existing.setAccessCount(existing.getAccessCount() + entry.count);
                        }, () -> repo.save(new DocumentAccessStat(entry.documentId, day, entry.count)));
            }
        }

        // archive file to prevent duplicate processing
        String archivedName = file.getFileName().toString().replace(".xml", ".processed.xml");
        Path target = archiveDir.resolve(archivedName);
        Files.move(file, target, StandardCopyOption.REPLACE_EXISTING);

        log.info("[BATCH][PERF] File {} done in {} ms (archived to {})",
                file.getFileName(), (System.currentTimeMillis() - tFile), target.getFileName());
    }
}
