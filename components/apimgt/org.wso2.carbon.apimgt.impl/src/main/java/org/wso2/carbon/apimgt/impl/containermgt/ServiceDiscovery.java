package org.wso2.carbon.apimgt.impl.containermgt;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Endpoint;
import org.wso2.carbon.apimgt.api.model.ServiceDiscoveryConf;
import org.wso2.carbon.apimgt.api.model.ServiceDiscoveryConfigurations;
import org.wso2.carbon.apimgt.api.model.ServiceDiscoveryEndpoints;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface ServiceDiscovery  {

    /**
     * Initialize the class
     * @param implParametersDetails Map relating to the cluster information
     */
    void initManager(Map implParametersDetails);

    /**
     * List all services in the specified cluster
     */
    ServiceDiscoveryEndpoints listServices();

    /**
     * List the subset of the services according to the offset and limit value
     * @param offset starting index
     * @param limit max number of services returned
     *
     */
    ServiceDiscoveryEndpoints listSubSetOfServices( int offset, int limit) ;

    /**
     * @return total number of services
     */
    int getNumberOfServices();


}
