package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.internal.service.BlockApiService;
import org.wso2.carbon.apimgt.internal.service.utils.BlockConditionDBUtil;

import javax.ws.rs.core.Response;

public class BlockApiServiceImpl implements BlockApiService {

    private static final Log log = LogFactory.getLog(BlockApiServiceImpl.class);

    @Override
    public Response blockGet(MessageContext messageContext) throws APIManagementException {
        log.info("Retrieving block conditions");
        return Response.ok().entity(BlockConditionDBUtil.getBlockConditionsDTO()).build();
    }
}
