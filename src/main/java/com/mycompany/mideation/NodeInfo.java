package com.mycompany.mideation;

public class NodeInfo {

    private int id;
    private String nodeName;
    private String host;
    private int port;

    public NodeInfo(int id, String nodeName,
                    String host, int port) {

        this.id = id;
        this.nodeName = nodeName;
        this.host = host;
        this.port = port;
    }

    public int getId() {
        return id;
    }

    public String getNodeName() {
        return nodeName;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
