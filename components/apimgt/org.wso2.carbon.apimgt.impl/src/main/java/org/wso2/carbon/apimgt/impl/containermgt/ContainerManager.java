/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.impl.containermgt;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.DeploymentStatus;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;

import java.util.Map;

public interface ContainerManager {

    /**
     * Initialize the class
     * @param containerMgtInfoDetails Map relating to the cluster information
     */
    void initManager(JSONObject containerMgtInfoDetails);

    /**
     * Initial publish of the API in cluster
     * @param api API
     * @param apiIdentifier API Identifier
     * @throws UserStoreException
     * @throws RegistryException
     * @throws ParseException
     * @throws APIManagementException
     */
    void changeLCStateCreatedToPublished(API api, APIIdentifier apiIdentifier, Registry registry)
            throws UserStoreException, RegistryException, ParseException, APIManagementException;

    /**
     * Deletes the API from all the clusters it had been deployed
     * @param apiId API Identifier
     * @param containerMgtInfoDetails Clusters which the API has published
     */
    void deleteAPI(APIIdentifier apiId, JSONObject containerMgtInfoDetails);

    /**
     * Represents the LC change "Demote to created"
     * Deletes the API from clusters
     * @param apiId API Identifier
     * @param containerMgtInfoDetails Clusters which the API has published
     */
    void changeLCStatePublishedToCreated(APIIdentifier apiId, JSONObject containerMgtInfoDetails);

    /**
     * Re-deploy an API
     * @param apiId API Identifier
     * @param containerMgtInfoDetails Clusters which the API has published
     */
    void apiRepublish(API api, APIIdentifier apiId, Registry registry, JSONObject containerMgtInfoDetails)
            throws ParseException, RegistryException, APIManagementException;

    /**
     * Represent sthe LC change Block
     * Deletes the API from the clusters
     * @param apiId API Identifier
     * @param containerMgtInfoDetails Clusters which the API has published
     */
    void changeLCStateToBlocked(APIIdentifier apiId, JSONObject containerMgtInfoDetails);

    /**
     * Represents the LC change Blocked --> Republish
     * Redeploy the API CR with "override : false"
     * @param apiId API Identifier
     * @param containerMgtInfoDetails Clusters which the API has published
     * @param configMapName Name of the Config Map
     */
    void changeLCStateBlockedToRepublished(APIIdentifier apiId, JSONObject containerMgtInfoDetails,
                                           String[] configMapName);

    DeploymentStatus getPodStatus (APIIdentifier apiIdentifier, String clusterName);

}
