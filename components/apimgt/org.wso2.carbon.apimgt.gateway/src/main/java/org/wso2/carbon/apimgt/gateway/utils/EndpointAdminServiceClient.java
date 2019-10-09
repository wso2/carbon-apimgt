/*
* Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.gateway.utils;

import org.apache.axis2.AxisFault;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.endpoint.stub.types.EndpointAdminEndpointAdminException;
import org.wso2.carbon.endpoint.stub.types.EndpointAdminStub;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;

public class EndpointAdminServiceClient {

    private EndpointAdminStub endpointAdminStub;
    private static Log log = LogFactory.getLog(EndpointAdminServiceClient.class);
    private static final String backEndURL = "local:///services/";

    public EndpointAdminServiceClient() throws AxisFault {
        endpointAdminStub = new EndpointAdminStub(null, backEndURL + "EndpointAdmin");
    }

    /**
     * Add endpoint to the gateway
     *
     * @param endpointData Content of the endpoint file
     * @return True if the endpoint file is added
     * @throws AxisFault Thrown if an error occurred
     */
    public boolean addEndpoint(String endpointData) throws AxisFault {
        try {
            return endpointAdminStub.addEndpoint(endpointData);
        } catch (Exception e) {
            log.error("Error adding endpoint file to the gateway", e);
            throw new AxisFault("Error while adding the endpoint file" + e.getMessage(), e);
        }
    }

    /**
     * Add endpoint to the tenant
     *
     * @param endpointData Content of the endpoint file
     * @param tenantDomain Domain of the logged tenant
     * @return True if the endpoint file is added
     * @throws AxisFault Thrown if an error occurred
     */
    public boolean addEndpoint(String endpointData, String tenantDomain) throws AxisFault {
        try {
            return endpointAdminStub.addEndpointForTenant(endpointData, tenantDomain);
        } catch (Exception e) {
            log.error("Error adding endpoint file to the gateway", e);
            throw new AxisFault("Error while adding the endpoint file to tenant space" + e.getMessage(), e);
        }
    }

    /**
     * Delete endpoint from the gateway
     *
     * @param endpointName Name of the endpoint to be deleted
     * @return True if the endpoint file is deleted
     * @throws AxisFault Thrown if an error occurred
     */
    public boolean deleteEndpoint(String endpointName) throws AxisFault {
        try {
            // This check was added due to endpoint was saved in endpoint directory instead of saving inline with api
            // synapse file.
            if (checkEndpointExistBeforeDelete(endpointName, null)) {
                return endpointAdminStub.deleteEndpoint(endpointName);
            }
            return true;
        } catch (Exception e) {
            log.error("Error deleting endpoint from gateway", e);
            throw new AxisFault("Error while deleting the endpoint file" + e.getMessage(), e);
        }
    }

    /**
     * Delete endpoint from the tenant
     *
     * @param endpointName Name of the endpoint to be deleted
     * @param tenantDomain Domain of the logged tenant
     * @return True if the endpoint file is deleted
     * @throws AxisFault Thrown if an error occurred
     */
    public boolean deleteEndpoint(String endpointName, String tenantDomain) throws AxisFault {
        try {
            // This check was added due to endpoint was saved in endpoint directory instead of saving inline with api
            // synapse file.
            if (checkEndpointExistBeforeDelete(endpointName, tenantDomain)) {
                return endpointAdminStub.deleteEndpointForTenant(endpointName, tenantDomain);
            }
            return true;
        } catch (Exception e) {
            log.error("Error deleting endpoint file from gateway", e);
            throw new AxisFault("Error while deleting the endpoint file from tenant" + e.getMessage(), e);
        }
    }

    /**
     * Removes the existing endpoints of synapse config for updating them
     *
     * @param apiName Name of the API
     * @param apiVersion Version of the API
     * @param tenantDomain Domain of the logged tenant
     * @return True if endpoints are successfully removed for updating
     * @throws AxisFault Thrown if an error occurred
     */
    public boolean removeEndpointsToUpdate(String apiName, String apiVersion, String tenantDomain) throws AxisFault {
        boolean status = false;
        try {            
            ArrayList<Integer> arrayList = new ArrayList<>();
            String[] endpointNames;
            if (!StringUtils.isEmpty(tenantDomain)
                    && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                endpointNames = endpointAdminStub.getEndPointsNamesForTenant(tenantDomain);
            } else {
                endpointNames = endpointAdminStub.getEndPointsNames();
            }
            if(endpointNames == null) {
                log.debug("Endpoints not found while trying to remove endpoints prior to update.");
                return true;
            }
            Arrays.sort(endpointNames); //Sorting required for Binary Search
            arrayList.add(Arrays.binarySearch(endpointNames, apiName + "--v" +
                    apiVersion + "_API" + APIConstants.GATEWAY_ENV_TYPE_PRODUCTION + "Endpoint"));
            arrayList.add(Arrays.binarySearch(endpointNames, apiName + "--v" +
                    apiVersion + "_API" + APIConstants.GATEWAY_ENV_TYPE_SANDBOX + "Endpoint"));

            for (int index : arrayList) {
                if (index >= 0) { //If not found, don't delete
                    if (!StringUtils.isEmpty(tenantDomain)
                            && !MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
                        endpointAdminStub.deleteEndpointForTenant(endpointNames[index], tenantDomain);
                    } else {
                        endpointAdminStub.deleteEndpoint(endpointNames[index]);
                    }
                    status = true;
                } //No else as there should always be a file to delete
            }
        } catch (Exception e) {
            log.error("Error while removing endpoint/s for updating endpoint/s", e);
            throw new AxisFault("Error while removing endpoint/s for updating endpoint/s" + e.getMessage(), e);
        }
        return status;
    }

    protected void setEndpointAdminStub(EndpointAdminStub endpointAdminStub) {
        this.endpointAdminStub = endpointAdminStub;
    }

    private boolean checkEndpointExistBeforeDelete(String endpointName, String tenantDomain)
            throws RemoteException, EndpointAdminEndpointAdminException {
        String[] endpointNames;
        if (tenantDomain != null) {
            endpointNames = endpointAdminStub.getEndPointsNamesForTenant(tenantDomain);
        } else {
            endpointNames = endpointAdminStub.getEndPointsNames();
        }
        if (endpointNames != null) {
            for (String name : endpointNames) {
                if (name.equals(endpointName)) {
                    return true;
                }
            }
        }
        return false;
    }
}
