package com.mycompany.mideation;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class SFTPUploadManager {

    private static final Logger LOGGER = Logger.getLogger(SFTPUploadManager.class.getName());

    private final List<SFTPClient> clients = new ArrayList<>();
    private final ExecutorService  executor;

    // ─── Load all servers from properties ─────────────────────────────────────
    public SFTPUploadManager(String propertiesFile) throws IOException {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(propertiesFile)) {
            if (in == null) throw new FileNotFoundException("Not found: " + propertiesFile);
            props.load(in);
        }

        int count = Integer.parseInt(props.getProperty("sftp.servers.count", "1"));
        for (int i = 1; i <= count; i++) {
            String prefix = "sftp.server." + i + ".";
            clients.add(new SFTPClient(
                "SERVER-" + i,
                props.getProperty(prefix + "host"),
                Integer.parseInt(props.getProperty(prefix + "port", "22")),
                props.getProperty(prefix + "user"),
                props.getProperty(prefix + "password"),
                props.getProperty(prefix + "remote.dir", "/upload/")
            ));
        }

        this.executor = Executors.newFixedThreadPool(clients.size());
        LOGGER.info("Loaded " + clients.size() + " SFTP server(s)");
    }

    // ─── Upload list of files to all servers in parallel ──────────────────────
    public void uploadAll(List<String> filePaths) throws InterruptedException {
        List<Future<?>> futures = new ArrayList<>();

        for (SFTPClient client : clients) {
            futures.add(executor.submit(() -> {
                try (SFTPClient c = client) {
                    c.connect();
                    for (String path : filePaths) {
                        c.upload(path);
                    }
                    c.listFiles();
                } catch (Exception e) {
                    LOGGER.severe("[" + client.getServerName() + "] Failed: " + e.getMessage());
                }
            }));
        }

        // Wait for all uploads to finish
        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (ExecutionException e) {
                LOGGER.severe("Upload task error: " + e.getMessage());
            }
        }

        LOGGER.info("All uploads completed");
    }

    public void shutdown() {
        executor.shutdown();
    }
}