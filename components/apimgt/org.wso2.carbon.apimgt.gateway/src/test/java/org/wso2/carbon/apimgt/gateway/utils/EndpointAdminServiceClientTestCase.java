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
import org.wso2.carbon.endpoint.stub.types.EndpointAdminStub;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

public class EndpointAdminServiceClientTestCase {

    @Test
    public void testAddEndpoint() {
        EndpointAdminServiceClient endpointAdminServiceClient = null;
        try {
            endpointAdminServiceClient = new EndpointAdminServiceClient();
            EndpointAdminStub endpointAdminStub = Mockito.mock(EndpointAdminStub.class);
            Mockito.when(endpointAdminStub.addEndpoint(Mockito.anyString())).thenReturn(true);
            endpointAdminServiceClient.setEndpointAdminStub(endpointAdminStub);
        } catch (Exception e) {
            Assert.fail("Exception while testing addEndpoint");
        }
        try {
            endpointAdminServiceClient.addEndpoint(Mockito.anyString());
        } catch (AxisFault e) {
            Assert.fail("AxisFault while testing addEndpoint");
        }
        try {
            endpointAdminServiceClient.addEndpoint(Mockito.anyString(), Mockito.anyString());
        } catch (AxisFault e) {
            Assert.fail("AxisFault while testing addEndpoint");
        }
    }

    @Test
    public void testAddEndpointAxisFault() {
        EndpointAdminServiceClient endpointAdminServiceClient = null;
        try {
            endpointAdminServiceClient = new EndpointAdminServiceClient();
        } catch (AxisFault e) {
            //test for AxisFault
        }
        try {
            endpointAdminServiceClient.addEndpoint("epData");
        } catch (AxisFault e) {
            //test for AxisFault
        }
        try {
            endpointAdminServiceClient.addEndpoint("epData", "tenant");
        } catch (AxisFault e) {
            //test for AxisFault
        }
    }

    @Test
    public void testDeleteEndpoint() {
        EndpointAdminServiceClient endpointAdminServiceClient = null;
        try {
            endpointAdminServiceClient = new EndpointAdminServiceClient();
            EndpointAdminStub endpointAdminStub = Mockito.mock(EndpointAdminStub.class);
            Mockito.when(endpointAdminStub.deleteEndpoint(Mockito.anyString())).thenReturn(true);
            Mockito.when(endpointAdminStub.getEndPointsNames()).thenReturn(null);
            Mockito.when(endpointAdminStub.getEndPointsNamesForTenant(Mockito.anyString())).thenReturn(null);
            endpointAdminServiceClient.setEndpointAdminStub(endpointAdminStub);
        } catch (Exception e) {
            Assert.fail("Exception while testing deleteEndpoint");
        }
        try {
            endpointAdminServiceClient.deleteEndpoint("PizzaShackAPI--v1.0.0_APIproductionEndpoint");
        } catch (AxisFault e) {
            Assert.fail("AxisFault while testing deleteEndpoint");
        }
        try {
            endpointAdminServiceClient.deleteEndpoint("PizzaShackAPI--v1.0.0_APIproductionEndpoint", "wso2.com");
        } catch (AxisFault e) {
            Assert.fail("AxisFault while testing deleteEndpoint");
        }
    }

    @Test
    public void testCheckEndpointExistBeforeDelete() {
        EndpointAdminServiceClient endpointAdminServiceClient = null;
        String endpointName = "PizzaShackAPI--v1.0.0_APIproductionEndpoint";
        String[] endpointArray = { "PizzaShackAPI--v1.0.0_APIproductionEndpoint",
                "PizzaShackAPI--v1.0.0_APIsandboxEndpoint" };
        String tenantDomain = "wso2.com";
        try {
            endpointAdminServiceClient = new EndpointAdminServiceClient();
            EndpointAdminStub endpointAdminStub = Mockito.mock(EndpointAdminStub.class);
            Mockito.when(endpointAdminStub.deleteEndpoint(endpointName)).thenReturn(true);
            Mockito.when(endpointAdminStub.deleteEndpointForTenant(endpointName, tenantDomain)).thenReturn(true);
            Mockito.when(endpointAdminStub.getEndPointsNames()).thenReturn(endpointArray);
            Mockito.when(endpointAdminStub.getEndPointsNamesForTenant(tenantDomain)).thenReturn(endpointArray);
            endpointAdminServiceClient.setEndpointAdminStub(endpointAdminStub);
            Assert.assertTrue(endpointAdminServiceClient.deleteEndpoint(endpointName));
            Assert.assertTrue(endpointAdminServiceClient.deleteEndpoint(endpointName, tenantDomain));
            Assert.assertTrue(endpointAdminServiceClient.deleteEndpoint("nonExistEndpoint"));
            Assert.assertTrue(endpointAdminServiceClient.deleteEndpoint("nonExistEndpoint", tenantDomain));
        } catch (Exception e) {
            Assert.fail("Exception while testing CheckEndpointExistBeforeDelete");
        }
    }

    @Test
    public void testDeleteEndpointAxisFault() {
        EndpointAdminServiceClient endpointAdminServiceClient = null;
        try {
            endpointAdminServiceClient = new EndpointAdminServiceClient();
        } catch (AxisFault e) {
            //test for AxisFault
        }
        try {
            endpointAdminServiceClient.deleteEndpoint("epName");
        } catch (AxisFault e) {
            //test for AxisFault
        }
        try {
            endpointAdminServiceClient.deleteEndpoint("epName", "tenant");
        } catch (AxisFault e) {
            //test for AxisFault
        }
    }

    @Test
    public void testRemoveEndpointsToUpdate() throws Exception {
        EndpointAdminServiceClient endpointAdminServiceClient = null;
        APIIdentifier identifier = null;
        API api = null;
        try {
            endpointAdminServiceClient = new EndpointAdminServiceClient();
            EndpointAdminStub endpointAdminStub = Mockito.mock(EndpointAdminStub.class);
            String[] endpointNames = {"API1--v1.0.0_APIproductionEndpoint", "API1--v1.0.0_APIsandboxEndpoint"};
            identifier = new APIIdentifier("P1_API1_1.0.0");
            api = new API(identifier);
            Mockito.when(endpointAdminStub.getEndPointsNames()).thenReturn(endpointNames);
            Mockito.when(endpointAdminStub.getEndPointsNamesForTenant(Mockito.anyString()))
                    .thenReturn(endpointNames);
            Mockito.when(endpointAdminStub.deleteEndpoint(Mockito.anyString())).thenReturn(true);
            Mockito.when(endpointAdminStub.deleteEndpointForTenant(Mockito.anyString(), Mockito.anyString()))
                    .thenReturn(true);
            endpointAdminServiceClient.setEndpointAdminStub(endpointAdminStub);
        } catch (Exception e) {
            Assert.fail("Exception while testing removeEndpointsToUpdate");
        }
        try {
            endpointAdminServiceClient.removeEndpointsToUpdate(api.getId().getApiName(), api.getId().getVersion()
                    , "");
            endpointAdminServiceClient.removeEndpointsToUpdate(api.getId().getApiName(), api.getId().getVersion()
                    , null);
            endpointAdminServiceClient.removeEndpointsToUpdate(api.getId().getApiName(), api.getId().getVersion()
                    , MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
        } catch (AxisFault e) {
            Assert.fail("AxisFault while testing removeEndpointsToUpdate");
        }
        try {
            endpointAdminServiceClient.removeEndpointsToUpdate(api.getId().getApiName(), api.getId().getVersion()
                    , "tenant");
        } catch (AxisFault e) {
            Assert.fail("AxisFault while testing removeEndpointsToUpdate for tenant");
        }
    }

    @Test
    public void testRemoveEndpointsToUpdateAxisFault() throws Exception {
        EndpointAdminServiceClient endpointAdminServiceClient = null;
        APIIdentifier identifier = null;
        API api = null;
        try {
            endpointAdminServiceClient = new EndpointAdminServiceClient();
            identifier = new APIIdentifier("P1_API1_1.0.0");
            api = new API(identifier);
        } catch (AxisFault e) {
            //test for AxisFault
        }
        try {
            endpointAdminServiceClient.removeEndpointsToUpdate(api.getId().getApiName(), api.getId().getVersion()
                    , "");
        } catch (AxisFault e) {
            //test for AxisFault
        }
        try {
            endpointAdminServiceClient.removeEndpointsToUpdate(api.getId().getApiName(), api.getId().getVersion()
                    , "tenant");
        } catch (AxisFault e) {
            //test for AxisFault
        }
    }
}
