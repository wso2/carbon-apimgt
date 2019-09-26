package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.botDataAPI.BotDetectionData;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.rest.api.admin.BotDataApiService;
import org.wso2.carbon.apimgt.rest.api.admin.dto.EmailDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.sql.SQLException;
import java.util.List;

public class BotDataApiServiceImpl extends BotDataApiService {

    private static final Log log = LogFactory.getLog(BotDataApiServiceImpl.class);

    @Override
    public Response botDataAddEmailPost(EmailDTO body) {
        APIAdminImpl apiAdminImpl = new APIAdminImpl();
        try {
            apiAdminImpl.addBotDataEmailConfiguration(body.getEmail());
            return Response.ok().build();
        } catch (APIManagementException | SQLException e) {
            String errorMessage = "Error when sending email ";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response botDataDeleteEmailDelete(String uuid) {
        APIAdminImpl apiAdminImpl = new APIAdminImpl();
        try {
            apiAdminImpl.deleteBotDataEmailList(uuid);
            return Response.ok().build();
        } catch (APIManagementException | SQLException e) {
            String errorMessage = "Error when deleting email ";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response botDataGetEmailListGet(String tenantDomain) {
        try {
            List<BotDetectionData> emailList = APIAdminImpl.retrieveSavedBotDataEmailList();
            return Response.ok().entity(emailList).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error when getting email ";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
