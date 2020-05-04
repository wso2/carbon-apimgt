package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.internal.service.KeyTemplatesApiService;

import javax.ws.rs.core.Response;

public class KeyTemplatesApiServiceImpl implements KeyTemplatesApiService {

    @Override
    public Response keyTemplatesGet(MessageContext messageContext) throws APIManagementException {
        return Response.ok().entity(BlockConditionDBUtil.getKeyTemplates()).build();
    }
}
