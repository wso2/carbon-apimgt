package org.wso2.carbon.apimgt.gateway.throttling.temp;

import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;

/**
 * temproary class used to handle error messages
 * need to implement a new class based on the new balerina
 */
public class ErrorHandler implements CarbonCallback {
    @Override
    public void done(CarbonMessage carbonMessage) {
        carbonMessage.setHeader("Status", "500 u r doomed");
        carbonMessage.setHeader("test header", "this is sam ");
        System.out.print("Message is inside Error Handlerrrrrrr ");
    }
}
