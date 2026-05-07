package com.mycompany.mideation;

import java.io.*;

public class CdrProcessor {

    public void processFiles(String inputDirPath,
                             String mscFile,
                             String smscFile,
                             String pgwFile) {

        File inputDir = new File(inputDirPath);
        File[] files = inputDir.listFiles();

        if (files == null || files.length == 0) {
            System.out.println("No input files found.");
            return;
        }

        try (
            FileWriter mscWriter = new FileWriter(mscFile);
            FileWriter smscWriter = new FileWriter(smscFile);
            FileWriter pgwWriter = new FileWriter(pgwFile);
        ) {

            boolean headerWritten = false;

            for (File file : files) {

                if (!file.isFile()) continue;

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

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}