package com.mycompany.mideation;

import com.jcraft.jsch.*;
import java.io.File;

public class SftpUploader {

    public static void upload(String localFilePath, String host, int port) {

        String username = ConfigLoader.get("sftp.username");
        String password = ConfigLoader.get("sftp.password");
        String remoteDir = ConfigLoader.get("sftp.remote.dir");

        JSch jsch = new JSch();
        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            session = jsch.getSession(username, host, port);
            session.setPassword(password);

            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            try {
                channelSftp.cd(remoteDir);
            } catch (SftpException e) {
                if ("/upload/".equalsIgnoreCase(remoteDir)) {
                    channelSftp.cd("/Download/");
                } else {
                    throw e;
                }
            }

            File file = new File(localFilePath);
            if (!file.isFile() || file.length() == 0) {
                System.out.println("Skipping upload (missing or empty): " + localFilePath);
                return;
            }
            channelSftp.put(localFilePath, file.getName());

            System.out.println("Uploaded: " + file.getName() + " → " + host + ":" + port);

        } catch (Exception e) {
            System.out.println("Upload failed: " + localFilePath);
            e.printStackTrace();
        } finally {
            if (channelSftp != null) channelSftp.disconnect();
            if (session != null) session.disconnect();
        }
    }
}