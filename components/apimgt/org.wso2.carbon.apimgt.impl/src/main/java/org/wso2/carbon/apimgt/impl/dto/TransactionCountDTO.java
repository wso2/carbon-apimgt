package org.wso2.carbon.apimgt.impl.dto;

public class TransactionCountDTO {
    private String id;
    private String host;
    private String serverID;
    private String serverType;
    private int count;
    private String recordedTime;

    public TransactionCountDTO(String id, String host, String serverId, String serverType, int count,
            String recordedTime) {
        this.id = id;
        this.host = host;
        this.serverID = serverId;
        this.serverType = serverType;
        this.count = count;
        this.recordedTime = recordedTime;
    }

    public TransactionCountDTO() {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getServerID() {
        return serverID;
    }

    public void setServerID(String serverID) {
        this.serverID = serverID;
    }

    public String getServerType() {
        return serverType;
    }

    public void setServerType(String serverType) {
        this.serverType = serverType;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getRecordedTime() {
        return recordedTime;
    }

    public void setRecordedTime(String recordedTime) {
        this.recordedTime = recordedTime;
    }
}
