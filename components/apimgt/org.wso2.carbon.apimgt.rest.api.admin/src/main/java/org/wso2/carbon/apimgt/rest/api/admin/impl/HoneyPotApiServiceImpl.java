package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.honeypotAPI.HoneyPotAPIAlertData;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;



import java.sql.SQLException;
import java.util.List;

import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;

public class HoneyPotApiServiceImpl extends HoneyPotApiService {
    @Override
    public Response honeyPotDeleteAlertDataDelete(String messageID) throws APIManagementException, SQLException {
        APIAdminImpl apiAdminImpl = new APIAdminImpl();
        apiAdminImpl.deleteHoneyPotAlert(messageID);
        return Response.ok().build();
    }

    @Override
    public Response honeyPotGetAlertDataGet() throws APIManagementException {
        APIAdminImpl apiAdminImpl = new APIAdminImpl();
        List<HoneyPotAPIAlertData> alertData = apiAdminImpl.getAlert();
        return Response.ok().entity(alertData).build();
    }

    @Override
    public Response honeyPotGetEmailListGet(String tenantDomain) throws APIManagementException {
        String user = RestApiUtil.getLoggedInUsername();
        tenantDomain = MultitenantUtils.getTenantDomain(user);
        List<String> emailList = APIAdminImpl.retrieveSavedHoneyPotAPIAlertEmailList(tenantDomain);
        return Response.ok().entity(emailList).build();
    }

    @Override
    public Response honeyPotUpdateEmailListPut(EmailListDTO body, String tenantDomain) throws APIManagementException,
            SQLException {
        String user = RestApiUtil.getLoggedInUsername();
        tenantDomain = MultitenantUtils.getTenantDomain(user);
        APIAdminImpl apiAdminImpl = new APIAdminImpl();
        apiAdminImpl.addHoneyPotAPiEmailAlertConfiguration(StringUtils.join(body.getList(), ","), tenantDomain);
        return Response.ok().build();
    }
}
