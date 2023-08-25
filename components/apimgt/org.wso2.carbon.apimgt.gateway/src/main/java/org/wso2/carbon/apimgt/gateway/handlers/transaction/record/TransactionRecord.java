package org.wso2.carbon.apimgt.gateway.handlers.transaction.record;

import org.wso2.carbon.apimgt.gateway.handlers.transaction.util.TransactionCountConfig;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.UUID;

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

    private String id;
    private String host;
    private String serverID;
    private Integer count;
    private String recordedTime;

    public TransactionRecord(Integer count) {
        this.id = UUID.randomUUID().toString();
        this.host = localhost;
        this.serverID = server;
        this.count = count;
        this.recordedTime = new Timestamp(System.currentTimeMillis()).toString();
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

    public String getId() {
        return id;
    }
}
