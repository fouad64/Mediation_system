package com.mycompany.mideation;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class SFTPUploadManager {

    private static final Logger LOGGER = Logger.getLogger(SFTPUploadManager.class.getName());

    private final List<SFTPClient> clients = new ArrayList<>();
    private final ExecutorService executor;

    public SFTPUploadManager(String propertiesFile) throws IOException {

        Properties props = new Properties();

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(propertiesFile)) {
            if (in == null) throw new FileNotFoundException("Not found: " + propertiesFile);
            props.load(in);
        }

        int count = Integer.parseInt(props.getProperty("sftp.servers.count", "1"));

        for (int i = 1; i <= count; i++) {

            String p = "sftp.server." + i + ".";

            clients.add(new SFTPClient(
                    "SERVER-" + i,
                    props.getProperty(p + "host"),
                    Integer.parseInt(props.getProperty(p + "port")),
                    props.getProperty(p + "user"),
                    props.getProperty(p + "password"),
                    props.getProperty(p + "remote.dir", "/upload/")
            ));
        }

        executor = Executors.newFixedThreadPool(clients.size());
        LOGGER.info("Loaded " + clients.size() + " SFTP server(s)");
    }

    // ROUTED UPLOAD
    public void uploadAll(Map<Integer, List<String>> routedFiles) throws InterruptedException {

        List<Future<?>> futures = new ArrayList<>();

        for (SFTPClient client : clients) {

            int port = client.getPort();
            List<String> files = routedFiles.get(port);

            if (files == null || files.isEmpty()) continue;

            futures.add(executor.submit(() -> {

                try (SFTPClient c = client) {

                    c.connect();

                    for (String file : files) {
                        c.upload(file);
                    }

                    c.listFiles();

                } catch (Exception e) {
                    LOGGER.severe("[" + client.getServerName() + "] Failed: " + e.getMessage());
                }

            }));
        }

        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (ExecutionException e) {
                LOGGER.severe("Task error: " + e.getMessage());
            }
        }

        LOGGER.info("All routed uploads completed");
    }

    public void shutdown() {
        executor.shutdown();
    }
}