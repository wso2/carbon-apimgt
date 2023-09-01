package org.wso2.carbon.apimgt.gateway.inbound.websocket.utils;

import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityException;
import org.wso2.carbon.apimgt.gateway.inbound.InboundMessageContext;
import org.wso2.carbon.apimgt.gateway.inbound.websocket.InboundProcessorResponseDTO;

public interface Authenticator {

    InboundProcessorResponseDTO authenticate(InboundMessageContext inboundMessageContext, String authenticationType) throws APISecurityException;
}
