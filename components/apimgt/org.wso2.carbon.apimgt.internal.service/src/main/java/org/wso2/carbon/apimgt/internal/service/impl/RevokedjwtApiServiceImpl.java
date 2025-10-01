package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.internal.service.RevokedjwtApiService;
import org.wso2.carbon.apimgt.internal.service.utils.BlockConditionDBUtil;

import javax.ws.rs.core.Response;

public class RevokedjwtApiServiceImpl implements RevokedjwtApiService {

    private static final Log log = LogFactory.getLog(RevokedjwtApiServiceImpl.class);

    @Override
    public Response revokedjwtGet(MessageContext messageContext) throws APIManagementException {
        log.info("Retrieving revoked JWT events");
        return Response.ok().entity(BlockConditionDBUtil.getRevokedJWTEvents()).build();
    }
}
