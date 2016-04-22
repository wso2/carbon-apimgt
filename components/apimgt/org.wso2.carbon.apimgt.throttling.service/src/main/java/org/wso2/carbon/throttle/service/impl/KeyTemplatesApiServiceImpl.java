package org.wso2.carbon.throttle.service.impl;

import org.wso2.carbon.throttle.service.KeyTemplatesApiService;

import javax.ws.rs.core.Response;

public class KeyTemplatesApiServiceImpl extends KeyTemplatesApiService {
    @Override
    public Response keyTemplatesGet(){
        return Response.ok().entity(BlockConditionDBUtil.getKeyTemplates()).build();
    }
}
