package com.mycompany.mideation;
import com.jcraft.jsch.*; 
import java.io.*;
import java.util.Properties;
import java.util.logging.Logger;

public class SFTPClient implements AutoCloseable {

    private static final Logger LOGGER = Logger.getLogger(SFTPClient.class.getName());

    private final String host;
    private final int port;
    private final String user;
    private final String password;
    private final String remoteDir;

    private Session session;
    private ChannelSftp channel;

    // ─── Constructor: load from .properties ───────────────────────────────────
    public SFTPClient(String propertiesFile) throws IOException {
        Properties props = new Properties();
        try (InputStream in = getClass().getClassLoader().getResourceAsStream(propertiesFile)) {
            if (in == null)
                throw new FileNotFoundException("Properties file not found: " + propertiesFile);
            props.load(in);
        }
        this.host = props.getProperty("sftp.host");
        this.port = Integer.parseInt(props.getProperty("sftp.port", "22"));
        this.user = props.getProperty("sftp.user");
        this.password = props.getProperty("sftp.password");
        this.remoteDir = props.getProperty("sftp.remote.dir", "/upload/");
    }

    // ─── Connect ───────────────────────────────────────────────────────────────
    public void connect() throws JSchException {
        JSch jsch = new JSch();
        session = jsch.getSession(user, host, port);
        session.setPassword(password);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        channel = (ChannelSftp) session.openChannel("sftp");
        channel.connect();
        LOGGER.info("Connected to SFTP server: " + host + ":" + port);
    }

    // ─── Upload ────────────────────────────────────────────────────────────────
    public void upload(String localPath) throws SftpException {
        File file = new File(localPath);
        if (!file.exists()) {
            LOGGER.warning("File not found: " + localPath);
            return;
        }
        String remotePath = remoteDir + file.getName();
        channel.put(localPath, remotePath);
        LOGGER.info("Uploaded: " + file.getName() + " → " + remotePath);
    }

    // ─── Download ──────────────────────────────────────────────────────────────
    public void download(String remoteFileName, String localDir) throws SftpException {
        String remotePath = remoteDir + remoteFileName;
        String localPath = localDir + File.separator + remoteFileName;
        channel.get(remotePath, localPath);
        LOGGER.info("Downloaded: " + remoteFileName + " → " + localPath);
    }

    // ─── List files ────────────────────────────────────────────────────────────
    public void listFiles() throws SftpException {
        LOGGER.info("Files in " + remoteDir + ":");
        channel.ls(remoteDir).forEach(entry -> {
            ChannelSftp.LsEntry e = (ChannelSftp.LsEntry) entry;
            if (!e.getFilename().startsWith("."))
                LOGGER.info("  - " + e.getFilename());
        });
    }

    // ─── AutoCloseable (try-with-resources) ───────────────────────────────────
    @Override
    public void close() {
        if (channel != null && channel.isConnected())
            channel.disconnect();
        if (session != null && session.isConnected())
            session.disconnect();
        LOGGER.info("Disconnected from SFTP server");
    }
}