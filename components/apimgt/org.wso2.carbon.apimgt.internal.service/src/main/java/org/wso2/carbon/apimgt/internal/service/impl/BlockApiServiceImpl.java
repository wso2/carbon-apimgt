package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.internal.service.BlockApiService;

import javax.ws.rs.core.Response;

public class BlockApiServiceImpl implements BlockApiService {

    @Override
    public Response blockGet(MessageContext messageContext) throws APIManagementException {
        return Response.ok().entity(BlockConditionDBUtil.getBlockConditionsDTO()).build();
    }
}
