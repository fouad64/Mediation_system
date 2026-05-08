package com.mycompany.mideation;

import com.jcraft.jsch.*;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DownstreamDelivery {

    /**
     * Maps a source node name to the filtered file that represents it.
     * "sms-c" in the Nodes table corresponds to the smsc.csv filtered output.
     */
    private static final Map<String, String> SOURCE_TO_FILE = Map.of(
        "msc",   "filtered/msc.csv",
        "sms-c", "filtered/smsc.csv",
        "pgw",   "filtered/pgw.csv"
    );

    public static void deliver() {
        System.out.println("=== Downstream Delivery Phase ===");

        List<MediationRule> rules = loadRules();

        if (rules.isEmpty()) {
            System.out.println("No mediation rules found. Skipping delivery.");
            return;
        }

        for (MediationRule rule : rules) {
            System.out.println("Processing: " + rule);

            String localFile = SOURCE_TO_FILE.get(rule.getSourceName());

            if (localFile == null) {
                System.out.println("  No file mapping for source: " + rule.getSourceName() + " — skipping.");
                continue;
            }

            File f = new File(localFile);
            if (!f.exists()) {
                System.out.println("  File not found: " + localFile + " — skipping.");
                continue;
            }

            uploadToDestination(localFile, rule);
        }

        System.out.println("=== Downstream Delivery Complete ===");
    }

    // ------------------------------------------------------------------
    // DB query: joins mediation_rules with nodes (twice) to resolve
    // source name and full destination connection details in one query.
    // ------------------------------------------------------------------
    private static List<MediationRule> loadRules() {
        List<MediationRule> rules = new ArrayList<>();

        String sql = """
            SELECT
                mr.id              AS rule_id,
                src.name           AS source_name,
                dst.name           AS destination_name,
                dst.ip_address     AS dest_host,
                dst.port           AS dest_port,
                dst.username       AS dest_username,
                dst.password       AS dest_password
            FROM mediation_rules mr
            JOIN nodes src ON mr.source_node_id      = src.id
            JOIN nodes dst ON mr.destination_node_id = dst.id
            WHERE dst.type = 'DOWNSTREAM'
            ORDER BY mr.id
            """;

        try {
            Connection conn = DatabaseConnection.get();
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                rules.add(new MediationRule(
                    rs.getInt("rule_id"),
                    rs.getString("source_name"),
                    rs.getString("destination_name"),
                    rs.getString("dest_host"),
                    rs.getInt("dest_port"),
                    rs.getString("dest_username"),
                    rs.getString("dest_password")
                ));
            }

            rs.close();
            stmt.close();

        } catch (Exception e) {
            System.out.println("Failed to load mediation rules from database.");
            e.printStackTrace();
        }

        return rules;
    }

    // ------------------------------------------------------------------
    // SFTP upload — same JSch pattern as existing SftpUploader.java
    // ------------------------------------------------------------------
    private static void uploadToDestination(String localFilePath, MediationRule rule) {
        Session     session     = null;
        ChannelSftp channelSftp = null;

        try {
            JSch jsch = new JSch();

            session = jsch.getSession(rule.getDestUsername(), rule.getDestHost(), rule.getDestPort());
            session.setPassword(rule.getDestPassword());
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            String remoteDir = ConfigLoader.get("sftp.remote.dir");
            channelSftp.cd(remoteDir);

            File file = new File(localFilePath);
            channelSftp.put(localFilePath, file.getName());

            System.out.println("  Uploaded: " + file.getName()
                + " --> " + rule.getDestinationName()
                + " (" + rule.getDestHost() + ":" + rule.getDestPort() + ")");

        } catch (Exception e) {
            System.out.println("  Upload failed for rule #" + rule.getRuleId()
                + " (" + rule.getSourceName() + " --> " + rule.getDestinationName() + ")");
            e.printStackTrace();
        } finally {
            if (channelSftp != null) channelSftp.disconnect();
            if (session     != null) session.disconnect();
        }
    }
}
