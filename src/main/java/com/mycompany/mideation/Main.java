package com.mycompany.mideation;

import java.util.logging.Logger;

public class Main {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        // try-with-resources → auto disconnect when done
        try (SFTPClient client = new SFTPClient("sftp.properties")) {

            client.connect();

            // ── Upload ──
            client.upload("/home/fouad/Desktop/msc_cdr.csv");

            // ── List ──
            client.listFiles();

            // ── Download ──
            client.download("msc_cdr.csv", "/home/fouad/Downloads");

        } catch (Exception e) {
            LOGGER.severe("SFTP operation failed: " + e.getMessage());
        }
    }
}