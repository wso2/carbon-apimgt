package org.wso2.carbon.apimgt.gateway.handlers.transaction.record;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.UUID;

public class TransactionRecord {
    private static String localhost;
    private static String server;
    private static String type;

    static {
        try {
            localhost = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            localhost = "Unknown";
        }
    }

    private String id;
    private String serverType;
    private String host;
    private String serverID;
    private Integer count;
    private String recordedTime;

    public TransactionRecord(Integer count) {
        this.id = UUID.randomUUID().toString();
        this.host = localhost;
        this.serverID = server;
        this.serverType = type;
        this.count = count;
        this.recordedTime = new Timestamp(System.currentTimeMillis()).toString();
    }

    public static void init(String serverID, String serverType) {
        try {
            localhost = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            localhost = "Unknown";
        }
        TransactionRecord.server = serverID;
        TransactionRecord.type = serverType;
    }

    public String getId() {
        return id;
    }
    public String getHost() {
        return host;
    }
    public String getServerID() {
        return serverID;
    }
    public String getServerType() {
        return serverType;
    }
    public void setCount(Integer count) {
        this.count = count;
    }
    public Integer getCount() {
        return count;
    }
    public String getRecordedTime() {
        return recordedTime;
    }

}
