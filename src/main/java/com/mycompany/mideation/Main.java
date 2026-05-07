package com.mycompany.mideation;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        try {
            DBManager dbManager = new DBManager();
            CDRParser parser = new CDRParser();
            String downloadDir = "downloads";
            new File(downloadDir).mkdirs();

            // 1. Load Nodes from DB
            Map<Integer, NodeInfo> nodes = dbManager.getAllNodes();
            List<CDRData> allFilteredRecords = new ArrayList<>();

            // 2. Process Each UPSTREAM Node
            for (NodeInfo node : nodes.values()) {
                if (!"UPSTREAM".equals(node.type)) continue;

                LOGGER.info("Connecting to " + node.name + " (" + node.host + ":" + node.port + ")...");

                try (SFTPClient client = new SFTPClient(node.name, node.host, node.port, node.username, node.password, "/upload/")) {
                    client.connect();
                    
                    // List files in /upload/
                    // For simplicity, we assume all CSV files in /upload/ should be processed
                    // In a real scenario, you'd use client.getChannel().ls("/upload/")
                    
                    // Let's assume the files match the node name keywords as before
                    String keyword = node.name.split(" ")[0].toLowerCase(); // e.g. "msc"
                    
                    // Since we want to download from the container's /upload/ folder:
                    // We'll try to find common filenames if listFiles() is not enough
                    String[] potentialFiles = { "msc_cdr.csv", "sms-c_cdr2.csv", "pgw_cdr3.csv" };
                    
                    for (String fileName : potentialFiles) {
                        if (fileName.contains(keyword)) {
                            try (InputStream is = client.getStream(fileName);
                                 Reader reader = new InputStreamReader(is)) {
                                
                                List<CDRData> filtered;
                                if (fileName.contains("msc")) filtered = parser.parseMSC(reader);
                                else if (fileName.contains("sms-c")) filtered = parser.parseSMSC(reader);
                                else if (fileName.contains("pgw")) filtered = parser.parsePGW(reader);
                                else continue;
                                
                                allFilteredRecords.addAll(filtered);
                                LOGGER.info("Processed " + fileName + " from " + node.name + ": found " + filtered.size() + " records.");
                            } catch (Exception e) {
                                // File might not exist on this specific node, which is fine
                            }
                        }
                    }
                }
            }

            // 3. Save Unified File
            if (!allFilteredRecords.isEmpty()) {
                Path outputPath = Paths.get(downloadDir, "unified_filtered_cdrs.csv");
                writeFilteredCSV(allFilteredRecords, outputPath);
                LOGGER.info("Mediation Complete: Saved " + allFilteredRecords.size() + " total filtered records to " + outputPath);
            } else {
                LOGGER.warning("No records found to process.");
            }

        } catch (Exception e) {
            LOGGER.severe("Mediation Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void writeFilteredCSV(List<CDRData> dataList, Path outputPath) throws Exception {
        try (java.io.Writer writer = Files.newBufferedWriter(outputPath);
             org.apache.commons.csv.CSVPrinter csvPrinter = new org.apache.commons.csv.CSVPrinter(writer, 
                     org.apache.commons.csv.CSVFormat.DEFAULT.withHeader(
                             "cdr_type", "subscriber_id", "related_party_id", "start_time", "end_time", 
                             "duration_sec", "volume_uplink", "volume_downlink", "status", 
                             "network_element_id", "location_or_rat", "imei", "apn", "message_id"))) {
            
            for (CDRData d : dataList) {
                csvPrinter.printRecord(
                    d.cdrType, d.subscriberId, d.relatedPartyId, d.startTime, d.endTime,
                    d.durationSec, d.volumeUplink, d.volumeDownlink, d.status,
                    d.networkElementId, d.locationOrRat, d.imei, d.apn, d.messageId
                );
            }
            csvPrinter.flush();
        }
    }
}