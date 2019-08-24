package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.botDataAPI.BotDetectedData;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.rest.api.admin.BotDataApiService;
import org.wso2.carbon.apimgt.rest.api.admin.dto.EmailDTO;

import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

public class BotDataApiServiceImpl extends BotDataApiService {
    @Override
    public Response botDataAddEmailPost(EmailDTO body) throws APIManagementException, SQLException {
        APIAdminImpl apiAdminImpl = new APIAdminImpl();
        apiAdminImpl.addBotDataEmailConfiguration(body.getEmail());
        return Response.ok().build();
    }
    @Override
    public Response botDataDeleteEmailDelete(String uuid) throws APIManagementException, SQLException {
        APIAdminImpl apiAdminImpl = new APIAdminImpl();
        apiAdminImpl.deleteBotDataEmailList(uuid);
        return Response.ok().build();
    }
    @Override
    public Response botDataGetEmailListGet(String tenantDomain) throws APIManagementException {
        List<BotDetectedData> emailList = APIAdminImpl.retrieveSavedBotDataEmailList();
        return Response.ok().entity(emailList).build();

    }
}
