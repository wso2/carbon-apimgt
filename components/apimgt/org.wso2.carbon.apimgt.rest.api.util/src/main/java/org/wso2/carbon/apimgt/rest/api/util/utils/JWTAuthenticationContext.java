package org.wso2.carbon.apimgt.rest.api.util.utils;

import org.apache.cxf.message.*;
import java.util.HashMap;

public class JWTAuthenticationContext {

    public static Message toMessageContext(HashMap<String,Object> authContext) {
        Exchange exchange = new ExchangeImpl();
        exchange.putAll(authContext);
        exchange.setInMessage((Message) authContext);
        return exchange.getInMessage();
    }

    public static HashMap<String,Object> toJWTAuthenticationContext(Message message) {
        HashMap<String,Object> hashMap = new HashMap<>();
        message.forEach(hashMap::put);
        return hashMap;
    }
}
