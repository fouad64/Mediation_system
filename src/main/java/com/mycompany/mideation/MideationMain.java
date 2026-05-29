package com.mycompany.mideation;

public class MideationMain {

    public static void main(String[] args) {

        System.out.println("=== PIPELINE STARTED ===");

        // =========================
        // 2. PROCESS PHASE
        // =========================
        System.out.println("=== Processing Phase ===");

        String inputDir = ConfigLoader.get("input.dir");

        String mscFile = ConfigLoader.get("output.msc");
        String smscFile = ConfigLoader.get("output.smsc");
        String pgwFile = ConfigLoader.get("output.pgw");

        CdrProcessor processor = new CdrProcessor();
        if (!processor.processFiles(inputDir, mscFile, smscFile, pgwFile)) {
            System.out.println("=== PIPELINE SKIPPED (no input files in " + inputDir + ") ===");
            return;
        }

        // =========================
        // 3. UPLOAD PHASE
        // =========================
        System.out.println("=== Upload Phase ===");
        SftpUploader.upload(mscFile,
                ConfigLoader.get("sftp.msc.host"),
                ConfigLoader.getInt("sftp.msc.port"));

        SftpUploader.upload(smscFile,
                ConfigLoader.get("sftp.smsc.host"),
                ConfigLoader.getInt("sftp.smsc.port"));

        SftpUploader.upload(pgwFile,
                ConfigLoader.get("sftp.pgw.host"),
                ConfigLoader.getInt("sftp.pgw.port"));

        // =========================
        // 1. DOWNLOAD PHASE
        // =========================
        System.out.println("=== Download Phase ===");

        SftpDownloader.download("msc", "/upload/msc.csv", "./down/msc.csv");
        SftpDownloader.download("smsc", "/upload/smsc.csv", "./down/smsc.csv");
        SftpDownloader.download("pgw", "/upload/pgw.csv", "./down/pgw.csv");

        // =========================
        // 4. FILTER PHASE (DOWNLOADED)
        // =========================
        System.out.println("=== Filtering Downloaded Files ===");
        SftpFilter.filter("./down", "./filtered");

        // =========================
        // 5. ROUTING PHASE
        // =========================
        System.out.println("=== Routing Phase ===");
        DownstreamRouter.routeFilteredFiles("./filtered");

        // =========================
        // 6. CLEANUP PHASE
        // =========================
        System.out.println("=== Cleanup Phase ===");
        SftpRemoteCleaner.clearUploadDirectory("msc");
        SftpRemoteCleaner.clearUploadDirectory("smsc");
        SftpRemoteCleaner.clearUploadDirectory("pgw");
        cleanDirectory("./out");
        cleanDirectory("./down");
        cleanDirectory("./filtered");

        System.out.println("=== PIPELINE COMPLETED ===");
    }

    private static void cleanDirectory(String dirPath) {
        java.io.File dir = new java.io.File(dirPath);
        java.io.File[] files = dir.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                if (file.isFile()) {
                    if (file.delete()) {
                        System.out.println("Cleaned up temp file: " + file.getPath());
                    } else {
                        System.err.println("Failed to delete temp file: " + file.getPath());
                    }
                }
            }
        }
    }
}
