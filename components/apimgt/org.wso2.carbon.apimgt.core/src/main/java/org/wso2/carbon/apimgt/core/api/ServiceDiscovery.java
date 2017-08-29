package org.wso2.carbon.apimgt.core.api;

import org.wso2.carbon.apimgt.core.models.Endpoint;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;


public interface ServiceDiscovery {

    List<Endpoint> listServices() throws MalformedURLException;

    List<Endpoint> listServices(String namesapce) throws MalformedURLException;

    List<Endpoint> listServices(String namesapce, HashMap<String, String> criteria) throws MalformedURLException;

    List<Endpoint> listServices(HashMap<String, String> criteria) throws MalformedURLException;

}
