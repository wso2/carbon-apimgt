package org.wso2.carbon.apimgt.persistence;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PersistenceManagerConfiguration {
    private Map<String, List<String>> configuration = new ConcurrentHashMap<String, List<String>>();

    private static final Log log = LogFactory.getLog(PersistenceManagerConfiguration.class);

    public String getFirstProperty(String key) {
        List<String> value = configuration.get(key);
        if (value == null) {
            return null;
        }
        return value.get(0);
    }

}
