package org.wso2.carbon.apimgt.core.api;

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

    Boolean isEnabled();

    List<Endpoint> listServices() throws MalformedURLException;

    List<Endpoint> listServices(String namesapce) throws MalformedURLException;

    List<Endpoint> listServices(String namesapce, HashMap<String, String> criteria) throws MalformedURLException;

    List<Endpoint> listServices(HashMap<String, String> criteria) throws MalformedURLException;

}
