package org.wso2.carbon.apimgt.api.model.honeypotAPI;

import java.io.Serializable;

public class HoneyPotAPIAlertData implements Serializable {

   // private List<Pipeline> pipelines;
    private String messageID;
    private String apiMethod;
    private String headerSet;
    private String messageBody;
    private String clientIp;
    private String userName;
    private String emails;

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

    public String getUserName(){
        return userName;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }

    public String getEmails(){
        return emails;
    }

    public void setEmails(String emails){
        this.emails = emails;
    }

//    public List<Pipeline> getPipelines() {
//        return pipelines;
//    }
//
//    public void setPipelines(List<Pipeline> pipelines) {
//        this.pipelines = pipelines;
//    }

    @Override
    public String toString() {
        return "HoneyPotAPIAlertData [messageID=" + getMessageID() + ", apiMethod=" + getApiMethod()
                + ", headerSet=" + getHeaderSet() + ", messageBody=" + getMessageBody() + ", clientIp="
                + getClientIp() + "]";
    }
}
