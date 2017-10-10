package org.wso2.carbon.apimgt.core.api;

import org.wso2.carbon.apimgt.core.exception.ServiceDiscoveryException;
import org.wso2.carbon.apimgt.core.models.Endpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This interface allows discovering services in a cluster using a URL given in a config file
 * and filtering by namespace and criteria.
 * Can be used to implement discovery in clusters such as Kubernetes or OpenShift.
 */
public abstract class ServiceDiscoverer {

    protected HashMap<String, String> cmsSpecificParameters;
    protected String namespaceFilter;
    protected HashMap<String, String> criteriaFilter;

    protected int serviceEndpointIndex;
    protected List<Endpoint> servicesList;


    public ServiceDiscoverer(HashMap<String, String> cmsSpecificParameters) {
        this.cmsSpecificParameters = cmsSpecificParameters;
        this.namespaceFilter = this.cmsSpecificParameters.get("namespace");
        String criteriaString = this.cmsSpecificParameters.get("criteria");
        if (criteriaString != null) {
            String[] criteriaArray = criteriaString.split(",");
            HashMap<String, String> criteriaMap = new HashMap<>();
            for (String pair : criteriaArray) {
                String[] entry = pair.split("=");
                criteriaMap.put(entry[0].trim(), entry[1].trim());
            }
            this.criteriaFilter = criteriaMap;
        }
        servicesList = new ArrayList<>();
        serviceEndpointIndex = 0;
    }

    public String getNamespaceFilter() {
        return namespaceFilter;
    };

    public HashMap<String, String> getCriteriaFilter() {
        return criteriaFilter;
    };

    /**
     * To get list of endpoints without any filtering.
     *
     * @return List of Endpoints
     * @throws ServiceDiscoveryException If an error occurs while listing
     */
    public List<Endpoint> listServices() throws ServiceDiscoveryException {
        return servicesList;
    };

    /**
     * To get list of endpoints, with a specific namespace.
     *
     * @param namespace     Namespace of the expected endpoints
     * @return List of Endpoints with the specified namespace
     * @throws ServiceDiscoveryException If an error occurs while listing
     */
    public List<Endpoint> listServices(String namespace) throws ServiceDiscoveryException {
        return servicesList;
    };

    /**
     * To get list of endpoints, with a specific criteria.
     *
     * @param criteria    A criteria the endpoints should be filtered by
     * @return List of Endpoints with the specified criteria
     * @throws ServiceDiscoveryException If an error occurs while listing
     */
    public List<Endpoint> listServices(HashMap<String, String> criteria) throws ServiceDiscoveryException {
        return servicesList;
    };

    /**
     * To get list of endpoints, with a specific namespace and a criteria.
     *
     * @param namespace   Namespace of the expected endpoints
     * @param criteria    A criteria the endpoints should be filtered by
     * @return List of Endpoints with the specified namespace and criteria
     * @throws ServiceDiscoveryException If an error occurs while listing
     */
    public List<Endpoint> listServices(String namespace, HashMap<String, String> criteria)
            throws ServiceDiscoveryException {
        return servicesList;
    };

    protected Endpoint createEndpoint(String id, String name, String endpointConfig, Long maxTps,
                                    String type, String endpointSecurity, String applicableLevel) {
        Endpoint.Builder endpointBuilder = new Endpoint.Builder();
        endpointBuilder.id(id);
        endpointBuilder.name(name);
        endpointBuilder.endpointConfig(endpointConfig);
        endpointBuilder.maxTps(maxTps);
        endpointBuilder.type(type);
        endpointBuilder.security(endpointSecurity);
        endpointBuilder.applicableLevel(applicableLevel);

        serviceEndpointIndex++;
        return endpointBuilder.build();
    }

}
