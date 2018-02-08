package org.wso2.carbon.apimgt.gateway.utils;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.endpoint.stub.types.EndpointAdminStub;

public class EndpointAdminServiceClient {

    private EndpointAdminStub endpointAdminStub;
    private static Log log = LogFactory.getLog(EndpointAdminServiceClient.class);
    private static final String backEndURL = "local:///services/";

    public EndpointAdminServiceClient() throws AxisFault {
        endpointAdminStub = new EndpointAdminStub(null, backEndURL + "EndpointAdmin");
    }

    public boolean addEndpoint(String endpointData) throws AxisFault {
        try {
            return endpointAdminStub.addEndpoint(endpointData);
        } catch (Exception e) {
            log.error("Error adding endpoint file to the gateway", e);
            throw new AxisFault("Error while adding the endpoint file" + e.getMessage(), e);
        }
    }

    public boolean addEndpoint(String endpointData, String tenantDomain) throws AxisFault {
        try {
            return endpointAdminStub.addEndpointForTenant(endpointData, tenantDomain);
        } catch (Exception e) {
            log.error("Error adding endpoint file to the gateway", e);
            throw new AxisFault("Error while adding the endpoint file to tenant space" + e.getMessage(), e);
        }
    }

    public boolean deleteEndpoint(String endpointName) throws AxisFault {
        try {
            return endpointAdminStub.deleteEndpoint(endpointName);
        } catch (Exception e) {
            log.error("Error deleting endpoint from gateway", e);
            throw new AxisFault("Error while deleting the endpoint file" + e.getMessage(), e);
        }
    }

    public boolean deleteEndpoint(String endpointName, String tenantDomain) throws AxisFault {
        try {
            return endpointAdminStub.deleteEndpointForTenant(endpointName, tenantDomain);
        } catch (Exception e) {
            log.error("Error deleting endpoint file from gateway", e);
            throw new AxisFault("Error while deleting the endpoint file from tenant" + e.getMessage(), e);
        }
    }

    public String[] getEndPointsNames() throws AxisFault {
        try {
            return endpointAdminStub.getEndPointsNames();
        } catch (Exception e) {
            log.error("Error getting endpoint names from the gateway", e);
            throw new AxisFault("Error while obtaining the endpoint names" + e.getMessage(), e);
        }
    }

    public String[] getEndPointsNames(String tenantDomain) throws AxisFault {
        try {
            return endpointAdminStub.getEndPointsNamesForTenant(tenantDomain);
        } catch (Exception e) {
            log.error("Error getting endpoint names from the gateway", e);
            throw new AxisFault("Error while obtaining the endpoint names from tenant space"
                    + e.getMessage(), e);
        }
    }
}
