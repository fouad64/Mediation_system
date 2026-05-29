package com.mycompany.mideation;

import java.io.*;

public class CdrProcessor {

    public boolean processFiles(String inputDirPath,
                             String mscFile,
                             String smscFile,
                             String pgwFile) {

        File inputDir = new File(inputDirPath);
        File[] files = inputDir.listFiles();

        if (files == null || files.length == 0) {
            System.out.println("No input files found.");
            return false;
        }

        try (
            FileWriter mscWriter = new FileWriter(mscFile);
            FileWriter smscWriter = new FileWriter(smscFile);
            FileWriter pgwWriter = new FileWriter(pgwFile);
        ) {

            boolean headerWritten = false;

            for (File file : files) {

                if (!file.isFile()) continue;

                // Backup the original CDR file before processing
                try {
                    File backupDir = new File("./backup");
                    if (!backupDir.exists()) {
                        backupDir.mkdirs();
                    }
                    File backupFile = new File(backupDir, file.getName());
                    java.nio.file.Files.copy(file.toPath(), backupFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    System.out.println("Backup created: " + backupFile.getPath());
                } catch (IOException e) {
                    System.out.println("Failed to backup file: " + file.getName() + " - " + e.getMessage());
                }

                try (BufferedReader br = new BufferedReader(new FileReader(file))) {

                    String header = br.readLine();

                    if (!headerWritten && header != null) {
                        mscWriter.write(header + "\n");
                        smscWriter.write(header + "\n");
                        pgwWriter.write(header + "\n");
                        headerWritten = true;
                    }

                    String line;

                    while ((line = br.readLine()) != null) {

                        String[] data = line.split(",", -1);
                        String type = data[0];

                        switch (type) {
                            case "MSC":
                                mscWriter.write(line + "\n");
                                break;
                            case "SMSC":
                                smscWriter.write(line + "\n");
                                break;
                            case "PGW":
                                pgwWriter.write(line + "\n");
                                break;
                            default:
                                System.out.println("Unknown type: " + type);
                        }
                    }

                } catch (IOException e) {
                    System.out.println("Error reading file: " + file.getName());
                    e.printStackTrace();
                }

                // delete after processing
                if (file.delete()) {
                    System.out.println("Deleted: " + file.getName());
                }
            }

            System.out.println("Processing completed.");
            return true;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}