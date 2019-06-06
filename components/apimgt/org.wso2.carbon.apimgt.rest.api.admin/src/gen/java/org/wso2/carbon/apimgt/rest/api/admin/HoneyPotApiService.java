package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.dto.EmailListDTO;

import java.sql.SQLException;

import javax.ws.rs.core.Response;

public abstract class HoneyPotApiService {
    public abstract Response honeyPotDeleteAlertDataDelete(String messageID) throws APIManagementException, SQLException;

    public abstract Response honeyPotGetAlertDataGet() throws APIManagementException;

    public abstract Response honeyPotGetEmailListGet(String tenantDomain) throws APIManagementException;

    public abstract Response honeyPotUpdateEmailListPut(EmailListDTO body, String tenantDomain) throws APIManagementException, SQLException;
}

