package org.wso2.carbon.apimgt.cleanup.service.organizationPurge;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;

public class ApiPurge implements OrganizationPurge {

    protected String username;

    public ApiPurge(String username) {
        this.username = username;
    }

    public void deleteOrganization(String orgId) throws APIManagementException {
        APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(username);
        apiProvider.deleteOrganizationAPIList(orgId);
    }
}
