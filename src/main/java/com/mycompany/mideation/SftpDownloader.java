package com.mycompany.mideation;

import com.jcraft.jsch.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class SftpDownloader {

    public static void download(String type, String remoteFile, String localFile) {

        String host = ConfigLoader.get("sftp." + type + ".host");
        int port = ConfigLoader.getInt("sftp." + type + ".port");

        String user = ConfigLoader.get("sftp.username");
        String pass = ConfigLoader.get("sftp.password");

        Session session = null;
        ChannelSftp channelSftp = null;
        File local = new File(localFile);

        try {
            JSch jsch = new JSch();

            session = jsch.getSession(user, host, port);
            session.setPassword(pass);

            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();

            System.out.println("Downloading: " + remoteFile);

            try (OutputStream out = new FileOutputStream(localFile)) {
                channelSftp.get(remoteFile, out);
            }

            System.out.println("Downloaded → " + localFile);

        } catch (Exception e) {
            System.out.println("Download failed: " + type);
            if (local.exists() && !local.delete()) {
                System.err.println("Failed to remove incomplete file: " + localFile);
            }
            e.printStackTrace();
        } finally {
            if (channelSftp != null) channelSftp.disconnect();
            if (session != null) session.disconnect();
        }
    }
}