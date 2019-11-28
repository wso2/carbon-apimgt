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
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.endpoint.service.EndpointAdmin;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class EndpointAdminServiceProxyTestCase {

    @Test
    public void testAddEndpoint() {
        EndpointAdminServiceProxy endpointAdminServiceProxy = null;
        try {
            endpointAdminServiceProxy = new EndpointAdminServiceProxy(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            EndpointAdmin endpointAdmin = Mockito.mock(EndpointAdmin.class);
            Mockito.when(endpointAdmin.addEndpoint(Mockito.anyString())).thenReturn(true);
            endpointAdminServiceProxy.setEndpointAdmin(endpointAdmin);
        } catch (Exception e) {
            Assert.fail("Exception while testing addEndpoint");
        }
        try {
            endpointAdminServiceProxy.addEndpoint(Mockito.anyString());
        } catch (AxisFault e) {
            Assert.fail("AxisFault while testing addEndpoint");
        }
        try {
            endpointAdminServiceProxy.addEndpoint(Mockito.anyString());
        } catch (AxisFault e) {
            Assert.fail("AxisFault while testing addEndpoint");
        }
    }

    @Test
    public void testAddEndpointAxisFault() {
        EndpointAdminServiceProxy endpointAdminServiceProxy = null;
        endpointAdminServiceProxy = new EndpointAdminServiceProxy(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        try {
            endpointAdminServiceProxy.addEndpoint("epData");
        } catch (AxisFault e) {
            //test for AxisFault
        }
        try {
            endpointAdminServiceProxy.addEndpoint("epData");
        } catch (AxisFault e) {
            //test for AxisFault
        }
    }

    @Test
    public void testDeleteEndpoint() {
        EndpointAdminServiceProxy endpointAdminServiceProxy = null;
        try {
            endpointAdminServiceProxy = new EndpointAdminServiceProxy(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            EndpointAdmin endpointAdmin = Mockito.mock(EndpointAdmin.class);
            Mockito.when(endpointAdmin.deleteEndpoint(Mockito.anyString())).thenReturn(true);
            Mockito.when(endpointAdmin.getEndPointsNames()).thenReturn(null);
            Mockito.when(endpointAdmin.getEndPointsNamesForTenant(Mockito.anyString())).thenReturn(null);
            endpointAdminServiceProxy.setEndpointAdmin(endpointAdmin);
        } catch (Exception e) {
            Assert.fail("Exception while testing deleteEndpoint");
        }
        try {
            endpointAdminServiceProxy.deleteEndpoint("PizzaShackAPI--v1.0.0_APIproductionEndpoint");
        } catch (AxisFault e) {
            Assert.fail("AxisFault while testing deleteEndpoint");
        }
        try {
            endpointAdminServiceProxy.deleteEndpoint("PizzaShackAPI--v1.0.0_APIproductionEndpoint");
        } catch (AxisFault e) {
            Assert.fail("AxisFault while testing deleteEndpoint");
        }
    }

    @Test
    public void testCheckEndpointExistBeforeDelete() {
        EndpointAdminServiceProxy endpointAdminServiceProxy = null;
        String endpointName = "PizzaShackAPI--v1.0.0_APIproductionEndpoint";
        String[] endpointArray = { "PizzaShackAPI--v1.0.0_APIproductionEndpoint",
                "PizzaShackAPI--v1.0.0_APIsandboxEndpoint" };
        String tenantDomain = "wso2.com";
        try {
            endpointAdminServiceProxy = new EndpointAdminServiceProxy(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            EndpointAdmin endpointAdmin = Mockito.mock(EndpointAdmin.class);
            Mockito.when(endpointAdmin.deleteEndpoint(endpointName)).thenReturn(true);
            Mockito.when(endpointAdmin.deleteEndpointForTenant(endpointName, tenantDomain)).thenReturn(true);
            Mockito.when(endpointAdmin.getEndPointsNames()).thenReturn(endpointArray);
            Mockito.when(endpointAdmin.getEndPointsNamesForTenant(tenantDomain)).thenReturn(endpointArray);
            endpointAdminServiceProxy.setEndpointAdmin(endpointAdmin);
        } catch (Exception e) {
            Assert.fail("Exception while testing CheckEndpointExistBeforeDelete");
        }
    }

    @Test
    public void testDeleteEndpointAxisFault() {
        EndpointAdminServiceProxy endpointAdminServiceProxy = null;
        endpointAdminServiceProxy = new EndpointAdminServiceProxy(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        try {
            endpointAdminServiceProxy.deleteEndpoint("epName");
        } catch (AxisFault e) {
            //test for AxisFault
        }
        try {
            endpointAdminServiceProxy.deleteEndpoint("epName");
        } catch (AxisFault e) {
            //test for AxisFault
        }
    }

    @Test
    public void testRemoveEndpointsToUpdate() throws Exception {
        EndpointAdminServiceProxy endpointAdminServiceProxy = null;
        APIIdentifier identifier = null;
        API api = null;
        try {
            endpointAdminServiceProxy = new EndpointAdminServiceProxy(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            EndpointAdmin endpointAdmin = Mockito.mock(EndpointAdmin.class);
            String[] endpointNames = {"API1--v1.0.0_APIproductionEndpoint", "API1--v1.0.0_APIsandboxEndpoint"};
            identifier = new APIIdentifier("P1_API1_1.0.0");
            api = new API(identifier);
            Mockito.when(endpointAdmin.getEndPointsNames()).thenReturn(endpointNames);
            Mockito.when(endpointAdmin.getEndPointsNamesForTenant(Mockito.anyString()))
                    .thenReturn(endpointNames);
            Mockito.when(endpointAdmin.deleteEndpoint(Mockito.anyString())).thenReturn(true);
            Mockito.when(endpointAdmin.deleteEndpointForTenant(Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(true);
            endpointAdminServiceProxy.setEndpointAdmin(endpointAdmin);
        } catch (Exception e) {
            Assert.fail("Exception while testing removeEndpointsToUpdate");
        }
        try {
            endpointAdminServiceProxy.removeEndpointsToUpdate(api.getId().getApiName(), api.getId().getVersion());
            endpointAdminServiceProxy.removeEndpointsToUpdate(api.getId().getApiName(), api.getId().getVersion());
            endpointAdminServiceProxy.removeEndpointsToUpdate(api.getId().getApiName(), api.getId().getVersion());
        } catch (AxisFault e) {
            Assert.fail("AxisFault while testing removeEndpointsToUpdate");
        }
        try {
            endpointAdminServiceProxy.removeEndpointsToUpdate(api.getId().getApiName(), api.getId().getVersion());
        } catch (AxisFault e) {
            Assert.fail("AxisFault while testing removeEndpointsToUpdate for tenant");
        }
    }

    @Test
    public void testRemoveEndpointsToUpdateAxisFault() throws Exception {
        EndpointAdminServiceProxy endpointAdminServiceProxy = null;
        APIIdentifier identifier = null;
        API api = null;
        endpointAdminServiceProxy = new EndpointAdminServiceProxy(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        identifier = new APIIdentifier("P1_API1_1.0.0");
        api = new API(identifier);
        try {
            endpointAdminServiceProxy.removeEndpointsToUpdate(api.getId().getApiName(), api.getId().getVersion());
        } catch (AxisFault e) {
            //test for AxisFault
        }
        try {
            endpointAdminServiceProxy.removeEndpointsToUpdate(api.getId().getApiName(), api.getId().getVersion());
        } catch (AxisFault e) {
            //test for AxisFault
        }
    }
}
