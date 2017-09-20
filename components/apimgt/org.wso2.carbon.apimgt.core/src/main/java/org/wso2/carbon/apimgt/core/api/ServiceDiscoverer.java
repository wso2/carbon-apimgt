package org.wso2.carbon.apimgt.core.api;

import org.wso2.carbon.apimgt.core.exception.ServiceDiscoveryException;
import org.wso2.carbon.apimgt.core.models.Endpoint;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;

/**
 * This interface allows discovering services in a cluster using a URL given in a config file
 * and filtering by namespace and criteria.
 * Can be used to implement discovery in clusters such as Kubernetes or OpenShift.
 */
public interface ServiceDiscoverer {

    /**
     * To check whether service discovery is enabled in the configurations class.
     *
     * @return Whether service discovery is enabled
     */
    Boolean isEnabled();

    /**
     * To get list of endpoints without any filtering.
     *
     * @return List of Endpoints
     * @throws ServiceDiscoveryException If an error occurs while listing
     */
    List<Endpoint> listServices() throws ServiceDiscoveryException;

    /**
     * To get list of endpoints, with a specific namespace.
     *
     * @param namespace     Namespace of the expected endpoints
     * @return List of Endpoints with the specified namespace
     * @throws ServiceDiscoveryException If an error occurs while listing
     */
    List<Endpoint> listServices(String namespace) throws ServiceDiscoveryException;

    /**
     * To get list of endpoints, with a specific criteria.
     *
     * @param criteria    A criteria the endpoints should be filtered by
     * @return List of Endpoints with the specified criteria
     * @throws ServiceDiscoveryException If an error occurs while listing
     */
    List<Endpoint> listServices(HashMap<String, String> criteria) throws ServiceDiscoveryException;

    /**
     * To get list of endpoints, with a specific namespace and a criteria.
     *
     * @param namespace   Namespace of the expected endpoints
     * @param criteria    A criteria the endpoints should be filtered by
     * @return List of Endpoints with the specified namespace and criteria
     * @throws ServiceDiscoveryException If an error occurs while listing
     */
    List<Endpoint> listServices(String namespace, HashMap<String, String> criteria) throws ServiceDiscoveryException;
}
