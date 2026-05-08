package com.mycompany.mideation;

public class MediationRule {

    private int    ruleId;
    private String sourceName;        // e.g. "msc", "sms-c", "pgw"
    private String destinationName;   // e.g. "postpaid", "prepaid"
    private String destHost;
    private int    destPort;
    private String destUsername;
    private String destPassword;

    public MediationRule(int ruleId,
                         String sourceName,
                         String destinationName,
                         String destHost,
                         int destPort,
                         String destUsername,
                         String destPassword) {
        this.ruleId          = ruleId;
        this.sourceName      = sourceName;
        this.destinationName = destinationName;
        this.destHost        = destHost;
        this.destPort        = destPort;
        this.destUsername    = destUsername;
        this.destPassword    = destPassword;
    }

    public int    getRuleId()          { return ruleId; }
    public String getSourceName()      { return sourceName; }
    public String getDestinationName() { return destinationName; }
    public String getDestHost()        { return destHost; }
    public int    getDestPort()        { return destPort; }
    public String getDestUsername()    { return destUsername; }
    public String getDestPassword()    { return destPassword; }

    @Override
    public String toString() {
        return "Rule #" + ruleId + ": " + sourceName + " --> " + destinationName
               + " (" + destHost + ":" + destPort + ")";
    }
}
