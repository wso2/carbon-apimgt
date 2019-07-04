package org.wso2.carbon.apimgt.usage.publisher.dto;

import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class DataBridgeBotDataDTO extends BotDataDTO {

    public DataBridgeBotDataDTO(BotDataDTO botDataDTO){
        setCurrentTime(botDataDTO.getCurrentTime());
        setMessageID(botDataDTO.getMessageID());
        setApiMethod(botDataDTO.getApiMethod());
        setHeaderSet(botDataDTO.getHeaderSet());
        setMessageBody(botDataDTO.getMessageBody());
        setClientIp(botDataDTO.getClientIp());
    }

    public Object createPayload() {
        return new Object[] { getCurrentTime(),getMessageID(),getApiMethod(), getHeaderSet(), getMessageBody()
                ,getClientIp() };
    }

    /*
     *  This method validates null for any mandatory field
     *
     *  @return Alist of mandatory values which are null
     *
     * */
    public List<String> getMissingMandatoryValues() {

        List<String> missingMandatoryValues = new ArrayList<String>();
        if (getCurrentTime() == 0) {
            missingMandatoryValues.add("Current Time");
        }
        if (getMessageID()== null) {
            missingMandatoryValues.add("Message ID");
        }
        if (getApiMethod() == null) {
            missingMandatoryValues.add("API invoked method");
        }
        if (getHeaderSet() == null) {
            missingMandatoryValues.add("Header Set");
        }
        if (getMessageBody() == null) {
            missingMandatoryValues.add("Message Body");
        }
        if (getClientIp() == null) {
            missingMandatoryValues.add("Client IP");
        }
        return missingMandatoryValues;
    }

    @Override
    public String toString() {

        return "Current Time: " + getCurrentTime() +
                ", Message ID: " + getMessageID() + ", API invoked method: " + getApiMethod() +
                ", Header Set: " + getHeaderSet() + ", Message Body: " + getClientIp();
    }
}
