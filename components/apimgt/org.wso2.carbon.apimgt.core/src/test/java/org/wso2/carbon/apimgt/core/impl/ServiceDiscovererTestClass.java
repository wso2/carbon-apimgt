package org.wso2.carbon.apimgt.core.impl;

import org.wso2.carbon.apimgt.core.exception.ServiceDiscoveryException;
import org.wso2.carbon.apimgt.core.models.Endpoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A sample class to test org.wso2.carbon.apimgt.core.impl.APIPublisherImpl#discoverServiceEndpoints()
 */
public class ServiceDiscovererTestClass extends ServiceDiscoverer {

    @Override
    void initImpl(HashMap<String, String> implParameters) throws ServiceDiscoveryException {}

    @Override
    public List<Endpoint> listServices() throws ServiceDiscoveryException {
        List<Endpoint> endpointList = new ArrayList<>();
        endpointList.add(new Endpoint.Builder().build());
        return endpointList;
    }

    @Override
    public List<Endpoint> listServices(String namespace) throws ServiceDiscoveryException {
        List<Endpoint> endpointList = new ArrayList<>();
        endpointList.add(new Endpoint.Builder().build());
        return endpointList;
    }

    @Override
    public List<Endpoint> listServices(HashMap<String, String> criteria) throws ServiceDiscoveryException {
        List<Endpoint> endpointList = new ArrayList<>();
        endpointList.add(new Endpoint.Builder().build());
        return endpointList;
    }

    @Override
    public List<Endpoint> listServices(String namespace, HashMap<String, String> criteria)
            throws ServiceDiscoveryException {
        List<Endpoint> endpointList = new ArrayList<>();
        endpointList.add(new Endpoint.Builder().build());
        return endpointList;
    }
}
