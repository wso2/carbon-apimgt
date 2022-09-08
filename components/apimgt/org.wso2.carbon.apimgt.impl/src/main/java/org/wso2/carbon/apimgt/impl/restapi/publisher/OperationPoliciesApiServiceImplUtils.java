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

package org.wso2.carbon.apimgt.impl.restapi.publisher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.OperationPolicyData;
import org.wso2.carbon.apimgt.api.model.OperationPolicyDefinition;
import org.wso2.carbon.apimgt.api.model.OperationPolicySpecification;
import org.wso2.carbon.apimgt.impl.restapi.CommonUtils;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OperationPoliciesApiServiceImplUtils {

    private static final Log log = LogFactory.getLog(OperationPoliciesApiServiceImplUtils.class);

    private OperationPoliciesApiServiceImplUtils() {
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
     * @return True if policy was deleted successfully
     * @throws APIManagementException when an internal error occurs
     */
    public static boolean deleteCommonOperationPolicyByPolicyId(String policyId, String organization)
            throws APIManagementException {
        APIProvider apiProvider = CommonUtils.getLoggedInUserProvider();
        OperationPolicyData existingPolicy =
                apiProvider.getCommonOperationPolicyByPolicyId(policyId, organization, false);
        if (existingPolicy != null) {
            apiProvider.deleteOperationPolicyById(policyId, organization);
            if (log.isDebugEnabled()) {
                log.debug("The common operation policy " + policyId + " has been deleted");
            }
            return true;
        } else {
            throw new APIManagementException("Couldn't retrieve an existing API policy with ID: "
                    + policyId, ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_NOT_FOUND,
                    policyId));
        }
    }

    /**
     * @param name         Operation policy name
     * @param version      Operation policy version
     * @param organization Organization
     * @return List of operation policies
     * @throws APIManagementException when an internal error occurs
     */
    public static List<OperationPolicyData> getAllCommonOperationPolicies(String name, String version,
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
    public static List<OperationPolicyData> getAllCommonOperationPolicies(String organization)
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
    public static OperationPolicyData getCommonOperationPolicyByPolicyId(String operationPolicyId, String organization,
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
                    ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_NOT_FOUND_WITH_NAME_AND_VERSION, name, version));
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
