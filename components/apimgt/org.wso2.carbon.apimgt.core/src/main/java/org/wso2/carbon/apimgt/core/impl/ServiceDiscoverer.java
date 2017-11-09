/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.core.impl;

import org.wso2.carbon.apimgt.core.exception.ServiceDiscoveryException;
import org.wso2.carbon.apimgt.core.models.Endpoint;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants.ServiceDiscoveryConstants;

import java.util.HashMap;
import java.util.List;


/**
 * Abstract class to discover and list services in a cluster
 */
public abstract class ServiceDiscoverer {

    private String namespaceFilter;
    private HashMap<String, String> criteriaFilter;
    int serviceEndpointIndex;


    /**
     * Initializes the necessary parameters,
     * Must be called by all subclasses of Service Discoverer within their own overriding .init() methods
     *
     * @param implParameters  implementation parameters provided in the configuration
     * @throws ServiceDiscoveryException if an error occurs in the implementation's init method
     */
    public void init(HashMap<String, String> implParameters) throws ServiceDiscoveryException {
        this.namespaceFilter = implParameters.get(ServiceDiscoveryConstants.NAMESPACE);

        // Convert the criteria that is passed in as a string into a map and then set the instance criteriaFilter
        String criteriaString = implParameters.get(ServiceDiscoveryConstants.CRITERIA);
        if (criteriaString != null) {
            String[] criteriaArray = criteriaString.split(",");
            HashMap<String, String> criteriaMap = new HashMap<>();
            for (String pair : criteriaArray) {
                String[] entry = pair.split("=");
                criteriaMap.put(entry[0].trim(), entry[1].trim());
            }
            this.criteriaFilter = criteriaMap;
        }
        serviceEndpointIndex = 0;
        initImpl(implParameters);
    }

    abstract void initImpl(HashMap<String, String> implParameters) throws ServiceDiscoveryException;

    /**
     * Gives a list of endpoints without any filtering.
     *
     * @return List of all Endpoints
     * @throws ServiceDiscoveryException if an error occurs while listing
     */
    public abstract List<Endpoint> listServices() throws ServiceDiscoveryException;

    /**
     * Gives a list of endpoints filtered by namespace.
     *
     * @param namespace     Namespace of the expected endpoints
     * @return List of Endpoints with the specified namespace
     * @throws ServiceDiscoveryException if an error occurs while listing
     */
    public abstract List<Endpoint> listServices(String namespace) throws ServiceDiscoveryException;

    /**
     * Gives a list of endpoints filtered by criteria.
     *
     * @param criteria    A set of criteria, all the endpoints must belong to
     * @return List of Endpoints with the specified criteria
     * @throws ServiceDiscoveryException if an error occurs while listing
     */
    public abstract List<Endpoint> listServices(HashMap<String, String> criteria) throws ServiceDiscoveryException;

    /**
     * Gives a list of endpoints filtered by both namespace and criteria.
     *
     * @param namespace   Namespace of the expected endpoints
     * @param criteria    A set of criteria, all the endpoints must belong to
     * @return List of Endpoints with the specified namespace and criteria
     * @throws ServiceDiscoveryException if an error occurs while listing
     */
    public abstract List<Endpoint> listServices(String namespace, HashMap<String, String> criteria)
            throws ServiceDiscoveryException;

    /**
     * Builds an Endpoint
     * @param id                Temporary id to be used by the UI
     * @param name              Name of the service
     * @param endpointConfig    Json string containing endpoint URL, namespace, criteria
     * @param maxTps            MaxTps
     * @param type              Application level protocol (eg. http/https)
     * @param endpointSecurity  Json string about endpoint security necessarily including "enabled" boolean key
     * @param applicableLevel   Whether applicable level is global or production only
     * @return {@link org.wso2.carbon.apimgt.core.models.Endpoint} object
     */
    protected Endpoint buildEndpoint(String id, String name, String endpointConfig, Long maxTps,
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

    /**
     * Gives the namespace to filter by
     *
     * @return namespace
     */
    public String getNamespaceFilter() {
        return namespaceFilter;
    }

    /**
     * Gives the criteria to filter by
     *
     * @return criteria map
     */
    public HashMap<String, String> getCriteriaFilter() {
        return criteriaFilter;
    }

}
