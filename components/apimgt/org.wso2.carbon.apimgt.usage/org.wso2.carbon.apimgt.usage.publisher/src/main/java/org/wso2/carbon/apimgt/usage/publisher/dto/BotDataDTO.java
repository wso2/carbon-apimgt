package org.wso2.carbon.apimgt.usage.publisher.dto;

public class BotDataDTO {

    private long currentTime;
    private String messageID;
    private String apiMethod;
    private String headerSet;
    private String messageBody;
    private String clientIp;

    public long getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(long currentTime) {
        this.currentTime = currentTime;
    }

    public String getMessageID() {
        return messageID;
    }

    public void setMessageID(String messageIDl) {
        this.messageID = messageIDl;
    }

    public String getApiMethod() {
        return apiMethod;
    }

    public void setApiMethod(String apiMethod) {
        this.apiMethod = apiMethod;
    }

    public String getHeaderSet() {
        return headerSet;
    }

    public void setHeaderSet(String headerSet) {
        this.headerSet = headerSet;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

}


