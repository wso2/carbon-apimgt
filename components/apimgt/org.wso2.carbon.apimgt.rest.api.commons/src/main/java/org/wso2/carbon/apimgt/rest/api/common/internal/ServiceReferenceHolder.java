package org.wso2.carbon.apimgt.rest.api.common.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.kernel.configprovider.CarbonConfigurationException;
import org.wso2.carbon.kernel.configprovider.ConfigProvider;
//import org.wso2.msf4j.internal.DataHolder;

import java.util.Map;

/**
 * Class used to map and get the configurations from the deployment.yaml
 */
public class ServiceReferenceHolder {

    private static final Logger log = LoggerFactory.getLogger(ServiceReferenceHolder.class);
    private static ServiceReferenceHolder instance = new ServiceReferenceHolder();
    private ConfigProvider configProvider;
    private Map map = null;

    private ServiceReferenceHolder() {

    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    public void setConfigProvider(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    public Map getAPIMConfiguration() {
        try {
            if (configProvider != null) {
                map = configProvider.getConfigurationMap("wso2.carbon.apmigt.environments");
                // configProvider.getConfigurationMap()
            } else {
                log.error("Configuration provider is null");
            }
        } catch (CarbonConfigurationException e) {
            log.error("error getting config : org.wso2.carbon.apimgt.core.internal.APIMConfiguration", e);
        }



        return map;
    }
}
