package com.mycompany.mideation;

import com.jcraft.jsch.*;
import java.io.*;
import java.util.logging.Logger;

public class SFTPClient implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(SFTPClient.class.getName());

    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final String remoteDir;
    private final String serverName;

    private Session session;
    private ChannelSftp channel;

    public int getPort() {
        return port;
    }

    public SFTPClient(String serverName, String host, int port,
            String user, String password, String remoteDir) {
        this.serverName = serverName;
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.remoteDir = remoteDir;
    }

    public void connect() throws JSchException {
        JSch jsch = new JSch();
        session = jsch.getSession(user, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();
        channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        LOGGER.info("[" + serverName + "] Connected → " + host + ":" + port);
    }

    public void upload(String localPath) throws SftpException {
        File file = new File(localPath);
        if (!file.exists()) {
            LOGGER.warning("[" + serverName + "] File not found: " + localPath);
            return;
        }
        String remotePath = remoteDir + file.getName();
        channel.put(localPath, remotePath);
        LOGGER.info("[" + serverName + "] Uploaded: " + file.getName() + " → " + remotePath);
    }

    public void listFiles() throws SftpException {
        LOGGER.info("[" + serverName + "] Files in " + remoteDir + ":");
        channel.ls(remoteDir).forEach(entry -> {
            ChannelSftp.LsEntry e = (ChannelSftp.LsEntry) entry;
            if (!e.getFilename().startsWith("."))
                LOGGER.info("[" + serverName + "]   - " + e.getFilename());
        });
    }

    public String getServerName() {
        return serverName;
    }

    @Override
    public void close() {
        if (channel != null && channel.isConnected())
            channel.disconnect();
        if (session != null && session.isConnected())
            session.disconnect();
        LOGGER.info("[" + serverName + "] Disconnected");
    }
}