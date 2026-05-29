package com.mycompany.mideation;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

import java.util.Vector;

public class SftpRemoteCleaner {

    public static void clearUploadDirectory(String type) {
        String host = ConfigLoader.get("sftp." + type + ".host");
        int port = ConfigLoader.getInt("sftp." + type + ".port");
        String remoteDir = ConfigLoader.get("sftp.remote.dir");
        clearDirectory(host, port, remoteDir, type.toUpperCase());
    }

    public static void clearDirectory(String host, int port, String remoteDir, String label) {
        String username = ConfigLoader.get("sftp.username");
        String password = ConfigLoader.get("sftp.password");

        Session session = null;
        ChannelSftp channelSftp = null;

        try {
            JSch jsch = new JSch();
            session = jsch.getSession(username, host, port);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            channelSftp.cd(remoteDir);

            @SuppressWarnings("unchecked")
            Vector<ChannelSftp.LsEntry> entries = channelSftp.ls(".");
            for (ChannelSftp.LsEntry entry : entries) {
                String name = entry.getFilename();
                if (".".equals(name) || "..".equals(name) || entry.getAttrs().isDir()) {
                    continue;
                }
                channelSftp.rm(name);
                System.out.println("Removed remote file: " + label + " " + remoteDir + name);
            }

        } catch (Exception e) {
            System.out.println("Remote cleanup failed for " + label + ": " + e.getMessage());
        } finally {
            if (channelSftp != null) channelSftp.disconnect();
            if (session != null) session.disconnect();
        }
    }
}
