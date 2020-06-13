package org.wso2.carbon.apimgt.impl.clients;

import org.apache.axis2.AxisFault;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.mgt.stub.UserAdminStub;
import org.wso2.carbon.utils.CarbonUtils;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

public class UserAdminClient {
    private UserAdminStub userAdminServiceStub;

    public UserAdminClient() throws APIManagementException {
        APIManagerConfiguration config = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIManagerConfiguration();
        String serverURL = config.getFirstProperty(APIConstants.AUTH_MANAGER_URL);
        String serviceUrl = serverURL + "UserAdmin";
        try {
            userAdminServiceStub = new UserAdminStub(serviceUrl);
        } catch (AxisFault axisFault) {
            throw new APIManagementException(axisFault);
        }

    }

    public void changePassword(String userName, String newPassword) throws APIManagementException {

        try {
            String tenantDomain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().
                    getTenantId(tenantDomain);
            PrivilegedCarbonContext.startTenantFlow();
            boolean isTenantFlowStarted = true;
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(tenantDomain);
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);

            String adminUserName = ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getRealmConfiguration().getAdminUserName();
            String adminPassword = ServiceReferenceHolder.getInstance().getRealmService().getTenantUserRealm(tenantId)
                    .getRealmConfiguration().getAdminPassword();
            CarbonUtils.setBasicAccessSecurityHeaders(adminUserName, adminPassword,
                    userAdminServiceStub._getServiceClient());
            userAdminServiceStub.changePassword(userName, newPassword);
            if (isTenantFlowStarted) {
                PrivilegedCarbonContext.endTenantFlow();
            }
        } catch (Exception e) {
            handleException("Error occurred while changing the user password", e);
        }
    }


}
