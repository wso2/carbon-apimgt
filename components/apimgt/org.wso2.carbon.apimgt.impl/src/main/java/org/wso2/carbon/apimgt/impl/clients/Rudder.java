/*
 * Copyright (c) 2023 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.clients;

import feign.Feign;
import feign.gson.GsonDecoder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.clients.dto.NamespaceToOrgDetailsResponse;

/**
 * Contains the Rudder client implementation relevant to the  /api/v1/choreo/mappings/namespace-to-project/{namespace}
 * endpoint
 */
public class Rudder {

    private static Log log = LogFactory.getLog(Rudder.class);
    private static final RudderClient client = Feign.builder().decoder(new GsonDecoder())
            .target(RudderClient.class, APIConstants.RUDDER_ENDPOINT_URL);

    public static String getOrgIdFromNamespace(String namespace) throws APIManagementException {
        log.debug("Calling Rudder to obtain the organizationId for namespace: " + namespace);
        NamespaceToOrgDetailsResponse response = client.getOrgDetailsFromNamespace(namespace);
        if (response == null) {
            throw  new APIManagementException ("Could not obtain a response from Rudder for namespace: " + namespace,
                    ExceptionCodes.ORGANIZATION_NOT_FOUND);
        } else if (response.data == null) {
            throw  new APIManagementException ("Rudder response does not have a data field for namespace: " +
                    namespace, ExceptionCodes.ORGANIZATION_NOT_FOUND);
        } else if (response.data.organization_id == null) {
            throw  new APIManagementException ("Rudder response does not have an organizationId field for namespace: " +
                    namespace, ExceptionCodes.ORGANIZATION_NOT_FOUND);
        }
        log.debug("Obtained organizationId: " + response.data.organization_id + " for namespace: " + namespace);
        return response.data.organization_id;
    }
}
