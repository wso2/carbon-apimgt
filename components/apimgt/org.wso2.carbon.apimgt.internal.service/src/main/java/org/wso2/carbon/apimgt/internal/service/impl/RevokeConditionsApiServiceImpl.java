package org.wso2.carbon.apimgt.internal.service.impl;

import org.wso2.carbon.apimgt.internal.service.*;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.utils.BlockConditionDBUtil;

import javax.ws.rs.core.Response;


public class RevokeConditionsApiServiceImpl implements RevokeConditionsApiService {

    public Response revokeConditionsGet(MessageContext messageContext) {
        return Response.ok().entity(BlockConditionDBUtil.getRevokedConditions()).build();
    }
}
