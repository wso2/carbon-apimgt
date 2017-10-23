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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Abstract class to discover and list services in a cluster using a set of cms specific parameters
 * while filtering by namespace and/or criteria as provided in the configuration.
 */
public abstract class ServiceDiscoverer {

    protected HashMap<String, String> cmsSpecificParameters;
    private String namespaceFilter;
    private HashMap<String, String> criteriaFilter;

    protected int serviceEndpointIndex;
    protected List<Endpoint> servicesList;


    /**
     * Initializes the necessary parameters
     *
     * @param cmsSpecificParameters  container management specific parameters provided in the configuration
     * @throws ServiceDiscoveryException if an error occurs in the implementation's init method
     */
    public void init(HashMap<String, String> cmsSpecificParameters) throws ServiceDiscoveryException {
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

    /**
     * Lists the endpoints without any filtering.
     *
     * @return List of Endpoints
     * @throws ServiceDiscoveryException If an error occurs while listing
     */
    public abstract List<Endpoint> listServices() throws ServiceDiscoveryException;

    /**
     * Lists the endpoints with a specific namespace.
     *
     * @param namespace     Namespace of the expected endpoints
     * @return List of Endpoints with the specified namespace
     * @throws ServiceDiscoveryException If an error occurs while listing
     */
    public abstract List<Endpoint> listServices(String namespace) throws ServiceDiscoveryException;

    /**
     * Lists the endpoints with a specific criteria.
     *
     * @param criteria    A criteria the endpoints should be filtered by
     * @return List of Endpoints with the specified criteria
     * @throws ServiceDiscoveryException If an error occurs while listing
     */
    public abstract List<Endpoint> listServices(HashMap<String, String> criteria) throws ServiceDiscoveryException;

    /**
     * Lists the endpoints with a specific namespace and a criteria.
     *
     * @param namespace   Namespace of the expected endpoints
     * @param criteria    A criteria the endpoints should be filtered by
     * @return List of Endpoints with the specified namespace and criteria
     * @throws ServiceDiscoveryException If an error occurs while listing
     */
    public abstract List<Endpoint> listServices(String namespace, HashMap<String, String> criteria)
            throws ServiceDiscoveryException;

    /**
     * Build a Endpoint
     * @param id
     * @param name
     * @param endpointConfig
     * @param maxTps
     * @param type
     * @param endpointSecurity
     * @param applicableLevel
     * @return
     */
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

    /**
     * Returns the namespace, provided in the configuration, to filter by
     *
     * @return Namespace to filter by
     */
    public String getNamespaceFilter() {
        return namespaceFilter;
    }

    /**
     * Returns the criteria, provided in the configuration, to filter by
     *
     * @return Criteria to filter by
     */
    public HashMap<String, String> getCriteriaFilter() {
        return criteriaFilter;
    }

}
