package org.wso2.carbon.throttle.service.impl;

import org.wso2.carbon.throttle.service.*;
import org.wso2.carbon.throttle.service.dto.*;


import org.wso2.carbon.throttle.service.dto.BlockConditionsDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public class BlockApiServiceImpl extends BlockApiService {
    @Override
    public Response blockGet(){

        return Response.ok().entity(BlockConditionDBUtil.getBlockConditionsDTO()).build();
    }
}
