package com.mycompany.mideation;

import java.io.*;

public class SftpFilter {

    public static void filter(String inputDir, String outputDir) {

        File downFolder = new File(inputDir);
        File filteredFolder = new File(outputDir);

        // 1. Create the filtered folder if it doesn't exist
        if (!filteredFolder.exists()) {
            filteredFolder.mkdirs();
        }

        // 2. Get list of files in the down folder
        File[] files = downFolder.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("No files found in down folder.");
            return;
        }

        // 3. Define output files
        File mscOut = new File(filteredFolder, "msc.csv");
        File smscOut = new File(filteredFolder, "smsc.csv");
        File pgwOut = new File(filteredFolder, "pgw.csv");

        // 4. Use simple Writers to create the files
        try {
            PrintWriter mscWriter = new PrintWriter(new FileWriter(mscOut));
            PrintWriter smscWriter = new PrintWriter(new FileWriter(smscOut));
            PrintWriter pgwWriter = new PrintWriter(new FileWriter(pgwOut));

            boolean headerSaved = false;

            for (File f : files) {
                // Only process .csv files
                if (f.getName().endsWith(".csv")) {
                    System.out.println("Filtering: " + f.getName());

                    BufferedReader reader = new BufferedReader(new FileReader(f));
                    String line;

                    // Read the header first
                    String header = reader.readLine();
                    if (!headerSaved && header != null) {
                        mscWriter.println(header);
                        smscWriter.println(header);
                        pgwWriter.println(header);
                        headerSaved = true;
                    }

                    // Read each data line
                    while ((line = reader.readLine()) != null) {
                        String[] parts = line.split(",", -1);
                        if (parts.length < 9)
                            continue;

                        String type = parts[0].trim();

                        // Logic for MSC
                        if (type.equalsIgnoreCase("MSC")) {
                            String calling = parts[1].trim();
                            String called = parts[2].trim();
                            String durationStr = parts[5].trim();
                            
                            int duration = 0;
                            try {
                                duration = durationStr.isEmpty() ? 0 : Integer.parseInt(durationStr);
                            } catch (Exception e) {
                                duration = 0; // treat bad data as 0
                            }
                            
                            // Check status (it might be in column 8 or 9)
                            String status = parts[8].trim();
                            if (status.isEmpty() && parts.length > 9) {
                                status = parts[9].trim();
                            }

                            // Rule: duration > 0 AND not short code AND status must be normal
                            if (duration > 0 && !isShort(calling) && !isShort(called)
                                    && status.equalsIgnoreCase("normal")) {
                                mscWriter.println(line);
                            }
                        }
                        // Logic for PGW
                        else if (type.equalsIgnoreCase("PGW")) {
                            String upStr = parts[6].trim();
                            String downStr = parts[7].trim();
                            
                            long up = 0;
                            long down = 0;
                            try {
                                up = upStr.isEmpty() ? 0 : Long.parseLong(upStr);
                                down = downStr.isEmpty() ? 0 : Long.parseLong(downStr);
                            } catch (Exception e) {
                                // treat bad data as 0
                            }

                            // Rule: must have some data (up or down > 0)
                            if (up > 0 || down > 0) {
                                pgwWriter.println(line);
                            }
                        }
                        // Logic for SMSC
                        else if (type.equalsIgnoreCase("SMSC")) {
                            String sender = parts[1].trim();
                            String receiver = parts[2].trim();
                            String status = parts[8].trim();

                            // Rule: not short number AND status must be delivered
                            if (!isShort(sender) && !isShort(receiver) && status.equalsIgnoreCase("delivered")) {
                                smscWriter.println(line);
                            }
                        }
                    }
                    reader.close();
                    f.delete(); // Delete original file after reading it
                }
            }

            // Close the output writers
            mscWriter.close();
            smscWriter.close();
            pgwWriter.close();

        } catch (IOException e) {
            System.out.println("Error during filtering: " + e.getMessage());
        }

        System.out.println("Filtering complete. Results saved to: " + outputDir);
    }

    private static boolean isShort(String number) {
        if (number.length() >= 2 && number.length() <= 6) {
            return true;
        }
        return false;
    }
}
