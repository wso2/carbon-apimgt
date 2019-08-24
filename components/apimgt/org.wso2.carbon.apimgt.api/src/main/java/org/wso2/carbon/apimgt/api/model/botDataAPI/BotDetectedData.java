package org.wso2.carbon.apimgt.api.model.botDataAPI;

public class BotDetectedData {

    private long currentTime;
    private String messageID;
    private String apiMethod;
    private String headerSet;
    private String messageBody;
    private String clientIp;
    private String tenantDomain;
    private String email;
    private String uuid;
    private String notificationType;

    public BotDetectedData(){

    }
    public  long getCurrentTime(){
        return currentTime;
    }

    public  void setCurrentTime(long currentTime){
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

    public void setApiMethod(String apiMethod){
        this.apiMethod = apiMethod;
    }

    public String getHeaderSet(){
        return headerSet;
    }

    public void setHeaderSet(String headerSet){
        this.headerSet = headerSet;
    }

    public String getMessageBody(){
        return messageBody;
    }

    public void setMessageBody(String messageBody){
        this.messageBody = messageBody;
    }

    public String getClientIp(){
        return clientIp;
    }

    public void setClientIp(String clientIp){
        this.clientIp = clientIp;
    }

    public String getTenantDomain(){
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain){
        this.tenantDomain = tenantDomain;
    }

    public String getEmail(){

        return email;
    }

    public void setEmail(String email){
            this.email = email;
    }

    public void setUuid(String uuid){
        this.uuid = uuid;
    }

    public String getUuid(){
        return uuid;
    }

    public void setNotificationType(String notificationType){
        this.notificationType = notificationType;
    }

    public String getNotificationType(){
        return notificationType;
    }

}
