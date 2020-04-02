package org.wso2.carbon.apimgt.impl.containermgt;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.model.Endpoint;
import org.wso2.carbon.apimgt.api.model.ServiceDiscoveryConf;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface ServiceDiscovery extends ContainerManager {
    void initManager(Map implParametersDetails);

    public ServiceDiscoveryEndpoints getServices(Map<String, Object> clusterProperties);
}
