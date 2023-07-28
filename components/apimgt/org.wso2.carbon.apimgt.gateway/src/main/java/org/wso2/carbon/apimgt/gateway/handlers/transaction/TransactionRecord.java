package org.wso2.carbon.apimgt.gateway.handlers.transaction;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;

public class TransactionRecord {

    private static String localhost;
    private static String server;

    static {
        server = TransactionCountConfig.getServerID();
        try {
             localhost = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            localhost = "Unknown";
        }
    }

    private String host;
    private String serverID;
    private Integer count;
    private String recordedTime;

    public TransactionRecord(Integer count) {
        this.host = localhost;
        this.serverID = server;
        this.count = count;
        this.recordedTime = Instant.now().toString();
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getHost() {
        return host;
    }

    public Integer getCount() {
        return count;
    }

    public String getRecordedTime() {
        return recordedTime;
    }

    public String getServerID() {
        return serverID;
    }
}
