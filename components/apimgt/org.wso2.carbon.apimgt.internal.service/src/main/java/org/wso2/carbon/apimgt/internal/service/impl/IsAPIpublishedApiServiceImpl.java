package org.wso2.carbon.apimgt.internal.service.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.internal.service.IsAPIpublishedApiService;
import org.apache.cxf.jaxrs.ext.MessageContext;
import javax.ws.rs.core.Response;


public class IsAPIpublishedApiServiceImpl implements IsAPIpublishedApiService {

    protected ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
    public Response isAPIpublishedGet(String apiId, MessageContext messageContext) throws APIManagementException {
        boolean status = false;
        try {
            status =  apiMgtDAO.isAPIPublishedInAnyGateway(apiId);
        } catch (APIManagementException e) {
            throw new APIManagementException("Error retrieving Artifact belongs to  " + apiId + " from DB", e);
        }
        return Response.ok().entity(status).build();
    }
}
