/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils;

import com.google.gson.JsonObject;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.dto.SolaceConfig;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.IntegratedAPIResponseDTO;
import org.wso2.carbon.apimgt.solace.api.v2.SolaceV2ApiHolder;
import org.wso2.carbon.apimgt.solace.api.v2.model.IntegratedSolaceApisResponse;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains utility methods related to Integrated APIs.
 */
public class IntegratedApiUtils {

    public static final String SOLACE = "solace";

    private IntegratedApiUtils() {
        // Prevents instantiation
    }

    /**
     * Gets a list of Integrated APIs for the given vendor.
     * @param vendor                    Vendor name
     * @return                          Response object
     * @throws APIManagementException   If an error occurs while getting the integrated APIs
     */
    public static Response getIntegratedApis(String vendor) throws APIManagementException {
        if (SOLACE.equals(vendor)) {
            return getSolaceEventApiProducts();
        }
        return getUnknownVendorResponse(vendor);
    }

    private static Response getSolaceEventApiProducts() throws APIManagementException {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfigurationService().getAPIManagerConfiguration();
        SolaceConfig solaceConfig = config.getSolaceConfig();
        if (solaceConfig != null && solaceConfig.isEnabled()) {
            IntegratedSolaceApisResponse solaceEventApiProductsResponse = SolaceV2ApiHolder.getInstance()
                    .getEventApiProducts();
            List<IntegratedAPIResponseDTO> integratedApis =
                    mapSolaceResponseToIntegratedApis(solaceEventApiProductsResponse);
            return Response.ok(integratedApis).build();
        }
        return getSolaceConfigsNotEnabledResponse();
    }

    private static List<IntegratedAPIResponseDTO> mapSolaceResponseToIntegratedApis(
            IntegratedSolaceApisResponse integratedSolaceApisResponse) {
        List<IntegratedAPIResponseDTO> integratedApis = new ArrayList<>();
        for (IntegratedSolaceApisResponse.EventApi integratedSolaceEventApi :
                integratedSolaceApisResponse.getIntegratedSolaceEventApis()) {
            IntegratedAPIResponseDTO integratedApi = new IntegratedAPIResponseDTO();
            integratedApi.setApiId(integratedSolaceEventApi.getApiId());
            integratedApi.setApiName(integratedSolaceEventApi.getApiName());
            integratedApi.setPlans(integratedSolaceEventApi.getPlans());
            integratedApis.add(integratedApi);
        }
        return integratedApis;
    }

    /**
     * Gets the definition of an Integrated API for the given vendor.
     * @param vendor                    Vendor name
     * @param params                    Query parameters related to the API definition retrieval
     * @return                          Response object
     * @throws APIManagementException   If an error occurs while getting the API definition
     */
    public static Response getIntegratedApiDefinition(String vendor, JSONObject params)
            throws APIManagementException {
        if (SOLACE.equals(vendor)) {
            return getSolaceApiDefinition(params);
        }
        return getUnknownVendorResponse(vendor);
    }

    private static Response getSolaceApiDefinition(JSONObject params) throws APIManagementException {
        // Validate required query parameters
        String eventApiProductId = params.get("eventApiProductId").toString();
        if (eventApiProductId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Query parameter 'eventApiProductId' is required").build();
        }
        String planId = params.get("planId").toString();
        if (planId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Query parameter 'planId' is required").build();
        }
        String eventApiId = params.get("eventApiId").toString();
        if (eventApiId == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Query parameter 'eventApiId' is required").build();
        }

        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        SolaceConfig solaceConfig = config.getSolaceConfig();
        if (solaceConfig != null && solaceConfig.isEnabled()) {
            JsonObject asyncApiDefinition = SolaceV2ApiHolder.getInstance()
                    .getEventApiAsyncApiDefinition(eventApiProductId, planId, eventApiId);
            return Response.ok().entity(asyncApiDefinition.toString()).build();
        }
        return getSolaceConfigsNotEnabledResponse();
    }

    private static Response getUnknownVendorResponse(String vendor) {
        return Response.status(Response.Status.BAD_REQUEST).entity("Unknown vendor " + vendor).build();
    }

    private static Response getSolaceConfigsNotEnabledResponse() {
        return Response.status(Response.Status.BAD_REQUEST).entity("Solace configs are not enabled").build();
    }
}
