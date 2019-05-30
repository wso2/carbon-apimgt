package org.wso2.carbon.apimgt.gateway.dto;

import java.util.ArrayList;
import java.util.List;

public class HoneyPotDTO {

    private String messageId;
    private String apiMethod;
    private String headerset;
    private String messageBody;
    private String clientIp;
    //private List <headerSet> headerSet=new ArrayList<headerSet>();

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getApiMethod() {
        return apiMethod;
    }

    public void setApiMethod(String apiMethod){
        this.apiMethod =apiMethod;
    }

    public String getHeaderset() {
        return headerset;
    }

    public void setHeaderset(String headerset){
        this.headerset = headerset;
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


}
