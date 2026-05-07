package com.mycompany.mideation;

import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        try {

            Path dir = Paths.get("cdr");

            // ── Read all CSV files
            List<String> allFiles;

            try (var stream = Files.list(dir)) {
                allFiles = stream
                        .filter(p -> p.toString().endsWith(".csv"))
                        .map(Path::toString)
                        .collect(Collectors.toList());
            }

            // Rout
            List<String> mscFiles = allFiles.stream()
                    .filter(f -> f.contains("msc"))
                    .toList();

            List<String> sms_cFiles = allFiles.stream()
                    .filter(f -> f.contains("sms-c"))
                    .toList();

            List<String> pgwFiles = allFiles.stream()
                    .filter(f -> f.contains("pgw"))
                    .toList();

            Map<Integer, List<String>> routed = new HashMap<>();
            routed.put(2221, mscFiles);
            routed.put(2222, sms_cFiles);
            routed.put(2223, pgwFiles);

            // Execute
            SFTPUploadManager manager = new SFTPUploadManager("sftp.properties");
            manager.uploadAll(routed);
            manager.shutdown();

        } catch (Exception e) {
            LOGGER.severe("Fatal error: " + e.getMessage());
        }
    }
}