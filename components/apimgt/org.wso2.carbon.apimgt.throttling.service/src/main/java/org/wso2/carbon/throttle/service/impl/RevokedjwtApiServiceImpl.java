package org.wso2.carbon.throttle.service.impl;

import org.wso2.carbon.throttle.service.RevokedjwtApiService;

import javax.ws.rs.core.Response;

public class RevokedjwtApiServiceImpl extends RevokedjwtApiService {
    @Override
    public Response revokedjwtGet(String query){
        return Response.ok().entity(BlockConditionDBUtil.getRevokedJWTs()).build();
    }
}
