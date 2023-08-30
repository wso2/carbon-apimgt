package org.wso2.carbon.apimgt.gateway.handlers.transaction.config;

import org.wso2.carbon.apimgt.gateway.handlers.transaction.exception.TransactionCounterInitializationException;

import java.util.HashMap;

public class MIConfigFetcher implements ConfigFetcher {

    private static MIConfigFetcher instance = null;
    private static HashMap<String, Object> configMap = new HashMap<>();

    private MIConfigFetcher() throws TransactionCounterInitializationException {
        // To be implemented
    }

    public static MIConfigFetcher getInstance() throws TransactionCounterInitializationException{
        if(instance == null) {
            instance = new MIConfigFetcher();
        }
        return instance;
    }

    @Override
    public String getConfigValue(String key) {
        return null;
    }
}
