package org.wso2.carbon.apimgt.block.service.impl;

import org.wso2.carbon.apimgt.block.service.*;


import javax.ws.rs.core.Response;

public class BlockApiServiceImpl extends BlockApiService {
    @Override
    public Response blockGet() {
        return Response.ok().entity(BlockConditionDBUtil.getBlockConditionsDTO()).build();
    }
}
