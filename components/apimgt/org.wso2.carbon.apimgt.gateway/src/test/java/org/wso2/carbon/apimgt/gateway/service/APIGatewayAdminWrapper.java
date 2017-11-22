package org.wso2.carbon.apimgt.gateway.service;


import org.apache.axis2.AxisFault;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.utils.MediationSecurityAdminServiceClient;
import org.wso2.carbon.apimgt.gateway.utils.RESTAPIAdminClient;
import org.wso2.carbon.apimgt.gateway.utils.SequenceAdminServiceClient;

public class APIGatewayAdminWrapper extends APIGatewayAdmin {
    private RESTAPIAdminClient restapiAdminClient;
    private SequenceAdminServiceClient sequenceAdminServiceClient;
    private MediationSecurityAdminServiceClient mediationSecurityAdminServiceClient;
    public APIGatewayAdminWrapper(RESTAPIAdminClient restapiAdminClient,SequenceAdminServiceClient sequenceAdminServiceClient) {

        this.restapiAdminClient = restapiAdminClient;
        this.sequenceAdminServiceClient = sequenceAdminServiceClient;
    }

    public APIGatewayAdminWrapper(RESTAPIAdminClient restapiAdminClient, SequenceAdminServiceClient
            sequenceAdminServiceClient, MediationSecurityAdminServiceClient mediationSecurityAdminServiceClient) {
        this.restapiAdminClient = restapiAdminClient;
        this.sequenceAdminServiceClient = sequenceAdminServiceClient;
        this.mediationSecurityAdminServiceClient = mediationSecurityAdminServiceClient;
    }

    @Override
    protected RESTAPIAdminClient getRestapiAdminClient(String apiProviderName, String apiName, String version) throws
            AxisFault {
        return restapiAdminClient;
    }

    @Override
    protected SequenceAdminServiceClient getSequenceAdminServiceClient() throws AxisFault {
        return sequenceAdminServiceClient;
    }

    @Override
    protected void deleteRegistryProperty(String apiProviderName, String apiName, String version, String
            tenantDomain) throws APIManagementException {
    }

    @Override
    protected void setRegistryProperty(String tenantDomain, String secureVaultAlias, String encodedValue) throws
            APIManagementException {
    }

    @Override
    protected MediationSecurityAdminServiceClient getMediationSecurityAdminServiceClient() throws AxisFault {
        return mediationSecurityAdminServiceClient;
    }
}
