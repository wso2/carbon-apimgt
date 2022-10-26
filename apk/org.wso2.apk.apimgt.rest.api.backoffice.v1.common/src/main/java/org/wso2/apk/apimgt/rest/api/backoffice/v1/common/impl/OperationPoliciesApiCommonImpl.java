/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.apk.apimgt.rest.api.backoffice.v1.common.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.apk.apimgt.api.APIProvider;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.model.OperationPolicyData;
import org.wso2.apk.apimgt.api.model.OperationPolicyDefinition;
import org.wso2.apk.apimgt.api.model.OperationPolicySpecification;
import org.wso2.apk.apimgt.impl.APIConstants;
import org.wso2.apk.apimgt.impl.importexport.ImportExportConstants;
import org.wso2.apk.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.apk.apimgt.impl.restapi.CommonUtils;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiConstants;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.common.mappings.OperationPolicyMappingUtil;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.OperationPolicyDataDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.OperationPolicyDataListDTO;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility class for operations related to OperationPoliciesApiService
 */
public class OperationPoliciesApiCommonImpl {

    private static final Log log = LogFactory.getLog(OperationPoliciesApiCommonImpl.class);

    private OperationPoliciesApiCommonImpl() {

    }

    /**
     * @param policySpecification Operation policy spec
     * @param organization        Validated organization
     * @return OperationPolicyData object
     */
    public static OperationPolicyData prepareOperationPolicyData(OperationPolicySpecification policySpecification,
                                                                 String organization) {

        OperationPolicyData operationPolicyData = new OperationPolicyData();
        operationPolicyData.setOrganization(organization);
        operationPolicyData.setSpecification(policySpecification);

        return operationPolicyData;
    }

    /**
     * @param policySpecification Operation policy spec
     * @param organization        Validated organization
     * @param apiId               API UUID
     * @return OperationPolicyData object
     */
    public static OperationPolicyData prepareOperationPolicyData(OperationPolicySpecification policySpecification,
                                                                 String organization, String apiId) {

        OperationPolicyData operationPolicyData = new OperationPolicyData();
        operationPolicyData.setOrganization(organization);
        operationPolicyData.setApiUUID(apiId);
        operationPolicyData.setSpecification(policySpecification);

        return operationPolicyData;
    }

    public static OperationPolicySpecification getPolicySpecification(String fileContentType, String jsonContent)
            throws APIManagementException {

        try {
            if (APIConstants.YAML_CONTENT_TYPE.equals(fileContentType)) {
                jsonContent = CommonUtil.yamlToJson(jsonContent);
            }
            return APIUtil.getValidatedOperationPolicySpecification(jsonContent);
        } catch (IOException e) {
            String errorMessage = "Error occurred while validating the policy specification";
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, errorMessage));
        }
    }

    /**
     * @param policyData       Operation policy data
     * @param policyDefinition Operation policy definition object
     * @param definition       Policy definition
     * @param gatewayType      Policy gateway type
     */
    public static void preparePolicyDefinition(
            OperationPolicyData policyData, OperationPolicyDefinition policyDefinition,
            String definition, OperationPolicyDefinition.GatewayType gatewayType) {

        policyDefinition.setContent(definition);
        policyDefinition.setGatewayType(gatewayType);
        policyDefinition.setMd5Hash(APIUtil.getMd5OfOperationPolicyDefinition(policyDefinition));

        if (OperationPolicyDefinition.GatewayType.Synapse.equals(gatewayType)) {
            policyData.setSynapsePolicyDefinition(policyDefinition);
        } else if (OperationPolicyDefinition.GatewayType.ChoreoConnect.equals(gatewayType)) {
            policyData.setCcPolicyDefinition(policyDefinition);
        }

        policyData.setMd5Hash(APIUtil.getMd5OfOperationPolicy(policyData));
    }

    /**
     * @param policySpecification Operation policy specification
     * @param operationPolicyData Operation policy metadata
     * @param organization        Organization
     * @return Policy ID of the created policy
     * @throws APIManagementException when an internal error occurs
     */
    public static String addCommonOperationPolicy(OperationPolicySpecification policySpecification,
                                                  OperationPolicyData operationPolicyData, String organization)
            throws APIManagementException {

        String policyId;
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        OperationPolicyData existingPolicy =
                apiProvider.getCommonOperationPolicyByPolicyName(policySpecification.getName(),
                        policySpecification.getVersion(), organization, false);
        if (existingPolicy == null) {
            policyId = apiProvider.addCommonOperationPolicy(operationPolicyData, organization);
            if (log.isDebugEnabled()) {
                log.debug("A common operation policy has been added with name "
                        + policySpecification.getName());
            }
        } else {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_ALREADY_EXISTS,
                    policySpecification.getName(), policySpecification.getVersion()));
        }
        return policyId;
    }

    /**
     * @param policyId     Operation policy ID
     * @param organization Organization
     * @throws APIManagementException when an internal error occurs
     */
    public static void deleteCommonOperationPolicyByPolicyId(String policyId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        OperationPolicyData existingPolicy =
                apiProvider.getCommonOperationPolicyByPolicyId(policyId, organization, false);
        if (existingPolicy == null) {
            throw new APIManagementException("Couldn't retrieve an existing API policy with ID: " + policyId,
                    ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_NOT_FOUND, policyId));
        }
        apiProvider.deleteOperationPolicyById(policyId, organization);
        if (log.isDebugEnabled()) {
            log.debug("The common operation policy " + policyId + " has been deleted");
        }
    }

    public static OperationPolicyDataListDTO getAllCommonOperationPolicies(Integer limit, Integer offset, String query,
                                                                           String organization)
            throws APIManagementException {

        OperationPolicyDataListDTO policyListDTO;
        List<OperationPolicyData> commonOperationPolicyList;
        String name;
        String version;

        // If name & version are given, it returns the policy data
        if (query != null) {
            Map<String, String> queryParamMap = getQueryParams(query);
            name = queryParamMap.get(ImportExportConstants.POLICY_NAME);
            version = queryParamMap.get(ImportExportConstants.VERSION_ELEMENT);

            commonOperationPolicyList = getAllCommonOperationPolicyData(name, version, organization);
            policyListDTO = OperationPolicyMappingUtil.fromOperationPolicyDataListToDTO(
                    commonOperationPolicyList, 0, 1);
        } else {
            offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

            // Since policy definition is bit bulky, we don't query the definition unnecessarily.
            commonOperationPolicyList = getAllCommonOperationPolicyData(organization);

            // Set limit to the query param value or the count of all policies
            limit = limit != null ? limit : commonOperationPolicyList.size();
            policyListDTO = OperationPolicyMappingUtil.fromOperationPolicyDataListToDTO(commonOperationPolicyList,
                    offset, limit);
        }
        return policyListDTO;
    }

    public static OperationPolicyDataDTO getCommonOperationPolicyByPolicyId(String operationPolicyId,
                                                                            String organization)
            throws APIManagementException {

        OperationPolicyData existingPolicy = getCommonOperationPolicyDataByPolicyId(operationPolicyId, organization,
                false);
        return OperationPolicyMappingUtil.fromOperationPolicyDataToDTO(existingPolicy);
    }

    /**
     * @param name         Operation policy name
     * @param version      Operation policy version
     * @param organization Organization
     * @return List of operation policies
     * @throws APIManagementException when an internal error occurs
     */
    public static List<OperationPolicyData> getAllCommonOperationPolicyData(String name, String version,
                                                                            String organization)
            throws APIManagementException {

        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        OperationPolicyData policyData = apiProvider.getCommonOperationPolicyByPolicyName(name, version,
                organization, false);
        if (policyData != null) {
            List<OperationPolicyData> commonOperationPolicyLIst = new ArrayList<>();
            commonOperationPolicyLIst.add(policyData);
            return commonOperationPolicyLIst;
        } else {
            String apiManagementExceptionErrorMessage =
                    "Couldn't retrieve an existing common policy with Name: " + name + " and Version: "
                            + version;
            throw new APIMgtResourceNotFoundException(apiManagementExceptionErrorMessage,
                    ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_NOT_FOUND_WITH_NAME_AND_VERSION, name,
                            version));
        }
    }

    /**
     * @param organization Organization
     * @return List of operation policies
     * @throws APIManagementException when an internal error occurs
     */
    public static List<OperationPolicyData> getAllCommonOperationPolicyData(String organization)
            throws APIManagementException {

        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        return apiProvider.getAllCommonOperationPolicies(organization);
    }

    /**
     * @param operationPolicyId      Operation policy ID
     * @param organization           Organization
     * @param isWithPolicyDefinition Whether to return policy content
     * @return Operation policy
     * @throws APIManagementException when an internal error occurs
     */
    public static OperationPolicyData getCommonOperationPolicyDataByPolicyId(String operationPolicyId,
                                                                             String organization,
                                                                             boolean isWithPolicyDefinition)
            throws APIManagementException {

        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        OperationPolicyData existingPolicy =
                apiProvider.getCommonOperationPolicyByPolicyId(operationPolicyId, organization, isWithPolicyDefinition);
        if (existingPolicy != null) {
            return existingPolicy;
        } else {
            throw new APIManagementException("Couldn't retrieve an existing common policy with ID: "
                    + operationPolicyId, ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_NOT_FOUND,
                    operationPolicyId));
        }
    }

    public static OperationPolicyData getCommonOperationPolicyContentByPolicyId(String operationPolicyId,
                                                                                String organization)
            throws APIManagementException {

        return getCommonOperationPolicyDataByPolicyId(operationPolicyId, organization, true);
    }

    /**
     * @param name         Operation policy name
     * @param version      Operation policy version
     * @param organization Organization
     * @return Operation policy
     * @throws APIManagementException when an internal error occurs
     */
    public static OperationPolicyData exportOperationPolicy(String name, String version, String organization)
            throws APIManagementException {

        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        OperationPolicyData policyData = apiProvider.getCommonOperationPolicyByPolicyName(name, version, organization,
                true);
        if (policyData != null) {
            return policyData;
        } else {
            throw new APIManagementException(
                    "Couldn't retrieve an existing common policy with Name: " + name + " and Version: " + version,
                    ExceptionCodes.from(
                            ExceptionCodes.OPERATION_POLICY_NOT_FOUND_WITH_NAME_AND_VERSION, name, version));
        }
    }

    /**
     * @param query Request query
     * @return Map of query params
     */
    public static Map<String, String> getQueryParams(String query) {

        Map<String, String> queryParamMap = new HashMap<>();
        String[] queryParams = query.split(" ");
        for (String param : queryParams) {
            String[] keyVal = param.split(":");
            if (keyVal.length == 2) {
                queryParamMap.put(keyVal[0], keyVal[1]);
            }
        }

        return queryParamMap;
    }

}
