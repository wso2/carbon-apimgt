package org.wso2.carbon.apimgt.impl.containermgt;

import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.model.Endpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface ServiceDiscovery extends ContainerManager {
    void initManager(Map implParametersDetails);

    public JSONObject getServices(Map<String, String> clusterProperties);
}
