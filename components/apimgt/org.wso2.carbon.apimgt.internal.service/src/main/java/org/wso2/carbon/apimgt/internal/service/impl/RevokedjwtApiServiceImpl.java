package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.internal.service.RevokedjwtApiService;
import org.wso2.carbon.apimgt.internal.service.utils.BlockConditionDBUtil;

import javax.ws.rs.core.Response;

public class RevokedjwtApiServiceImpl implements RevokedjwtApiService {

    @Override
    public Response revokedjwtGet(MessageContext messageContext) throws APIManagementException {
        return Response.ok().entity(BlockConditionDBUtil.getRevokedJWTEvents()).build();
    }
}
