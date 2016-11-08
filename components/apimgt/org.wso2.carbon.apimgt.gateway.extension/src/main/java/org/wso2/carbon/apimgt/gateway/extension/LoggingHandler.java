package org.wso2.carbon.apimgt.gateway.extension;
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.messaging.CarbonCallback;
import org.wso2.carbon.messaging.CarbonMessage;
import org.wso2.carbon.messaging.handler.MessagingHandler;

/**
 * Messaging Handler implementation to log transport header in specified points
 */
@Component(
        name = "org.wso2.carbon.apimgt.gateway.extension.LoggingHandler",
        immediate = true,
        service = MessagingHandler.class)

public class LoggingHandler implements MessagingHandler {

    private Logger log = LoggerFactory.getLogger(LoggingHandler.class);

    @Override
    /**
     * request should have a header named "hello_continue" to pass the validation
     */
    public boolean validateRequestContinuation(CarbonMessage carbonMessage, CarbonCallback carbonCallback) {
        //String helloContinue = carbonMessage.getHeader("helloContinue");
        /*if (helloContinue == null) {
            DefaultCarbonMessage securityFailedCM = new DefaultCarbonMessage();
            String errMsg = "import header not found";
            securityFailedCM.setStringMessageBody(errMsg);
            Map<String, String> transportHeaders = new HashMap<>();
            transportHeaders.put(Constants.HTTP_CONNECTION, Constants.KEEP_ALIVE);
            transportHeaders.put(Constants.HTTP_CONTENT_TYPE, Constants.TEXT_PLAIN);
            transportHeaders.put(Constants.HTTP_CONTENT_LENGTH,
                    (String.valueOf(errMsg.getBytes(Charset.defaultCharset()).length)));

            securityFailedCM.setHeaders(transportHeaders);

            securityFailedCM.setProperty(Constants.HTTP_STATUS_CODE, 401);
            securityFailedCM.setProperty(Constants.DIRECTION, Constants.DIRECTION_RESPONSE);
            carbonCallback.done(securityFailedCM);

            return false;
        }*/
        return true;
    }

    @Override
    public void invokeAtSourceConnectionInitiation(String s) {

    }

    @Override
    public void invokeAtSourceConnectionTermination(String s) {

    }

    @Override
    public void invokeAtSourceRequestReceiving(CarbonMessage carbonMessage) {

    }

    @Override
    public void invokeAtSourceRequestSending(CarbonMessage carbonMessage) {
        //+ formatHeader(carbonMessage.getHeaders())
        log.info("headers in request receiving to engine: ");
    }

    @Override
    public void invokeAtTargetRequestReceiving(CarbonMessage carbonMessage) {
        log.info("headers in request sending from engine:");
        // + formatHeader(carbonMessage.getHeaders()));
    }

    @Override
    public void invokeAtTargetRequestSending(CarbonMessage carbonMessage) {

    }

    @Override
    public void invokeAtTargetResponseReceiving(CarbonMessage carbonMessage) {

    }

    @Override
    public void invokeAtTargetResponseSending(CarbonMessage carbonMessage) {
        log.info("headers in response receiving to engine: ");
        //+ formatHeader(carbonMessage.getHeaders()));
    }

    @Override
    public void invokeAtSourceResponseReceiving(CarbonMessage carbonMessage) {
        log.info("headers in response sending" + formatHeader(carbonMessage));
    }

    @Override
    public void invokeAtSourceResponseSending(CarbonMessage carbonMessage) {

    }

    @Override
    public void invokeAtTargetConnectionInitiation(String s) {

    }

    @Override
    public void invokeAtTargetConnectionTermination(String s) {

    }

    @Override
    public String handlerName() {
        return "logging handler";
    }

    private String formatHeader(CarbonMessage message) {
        String header = message.getHeader("Authorization");
        /*StringBuilder stringBuilder = new StringBuilder("\n");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            stringBuilder.append(entry.getKey());
            stringBuilder.append(" --> ");
            stringBuilder.append(entry.getValue());
            stringBuilder.append("\n");
        }*/
        return header;

    }
}
