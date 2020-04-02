package org.wso2.carbon.apimgt.impl.containermgt.k8service;

import org.wso2.carbon.apimgt.impl.containermgt.ContainerManager;

import java.util.Map;

public interface ServiceDiscovery extends ContainerManager {
    void initManager(Map implParametersDetails);

    public Service getServices(Map<String, Object> clusterProperties);
}
