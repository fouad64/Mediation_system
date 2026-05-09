package com.mycompany.mideation;

import java.io.File;
import java.util.List;

public class DownstreamRouter {

    public static void routeFilteredFiles(
            String filteredDir) {

        List<MediationRule> rules =
                DatabaseManager.getRules();

        for (MediationRule rule : rules) {

            // Get source node
            NodeInfo sourceNode =
                    DatabaseManager.getNodeById(
                            rule.getSourceNodeId());

            // Get destination node
            NodeInfo destinationNode =
                    DatabaseManager.getNodeById(
                            rule.getDestinationNodeId());

            if (sourceNode == null ||
                    destinationNode == null) {

                System.out.println(
                        "Invalid rule.");
                continue;
            }

            // Example:
            // MSC -> msc.csv
            String fileName =
                    sourceNode.getNodeName()
                            .toLowerCase()
                            + ".csv";

            File file =
                    new File(filteredDir, fileName);

            if (!file.exists()) {

                System.out.println(
                        "File not found: "
                                + fileName);

                continue;
            }

            System.out.println(
                    "Routing "
                            + fileName
                            + " → "
                            + destinationNode.getNodeName()
            );

            // Upload
            SftpUploader.upload(
                    file.getAbsolutePath(),
                    destinationNode.getHost(),
                    destinationNode.getPort()
            );
        }
    }
}