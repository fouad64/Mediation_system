package com.mycompany.mideation;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        // ── List of CSV files to upload ──────────────────────────────────────
        List<String> files = Arrays.asList(
            "/home/fouad/Desktop/msc_cdr.csv",
            "/home/fouad/Desktop/msc_cdr2.csv",
            "/home/fouad/Desktop/msc_cdr3.csv"
        );

        // ── Upload to all servers in parallel ────────────────────────────────
        try {
            SFTPUploadManager manager = new SFTPUploadManager("sftp.properties");
            manager.uploadAll(files);
            manager.shutdown();
        } catch (Exception e) {
            LOGGER.severe("Fatal error: " + e.getMessage());
        }
    }
}