package org.wso2.carbon.apimgt.gateway.utils;

import org.apache.axis2.*;
import org.junit.*;
import org.mockito.*;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.endpoint.stub.types.*;
import org.wso2.carbon.utils.multitenancy.*;

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
            endpointAdminServiceClient.setEndpointAdminStub(endpointAdminStub);
        } catch (Exception e) {
            Assert.fail("Exception while testing deleteEndpoint");
        }
        try {
            endpointAdminServiceClient.deleteEndpoint(Mockito.anyString());
        } catch (AxisFault e) {
            Assert.fail("AxisFault while testing deleteEndpoint");
        }
        try {
            endpointAdminServiceClient.deleteEndpoint(Mockito.anyString(), Mockito.anyString());
        } catch (AxisFault e) {
            Assert.fail("AxisFault while testing deleteEndpoint");
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
