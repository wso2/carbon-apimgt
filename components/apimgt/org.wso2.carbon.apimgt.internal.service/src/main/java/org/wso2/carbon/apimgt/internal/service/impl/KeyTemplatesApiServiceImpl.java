package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.internal.service.KeyTemplatesApiService;
import org.wso2.carbon.apimgt.internal.service.utils.BlockConditionDBUtil;

import javax.ws.rs.core.Response;

public class KeyTemplatesApiServiceImpl implements KeyTemplatesApiService {

    private static final Log log = LogFactory.getLog(KeyTemplatesApiServiceImpl.class);

    @Override
    public Response keyTemplatesGet(MessageContext messageContext) throws APIManagementException {
        log.info("Retrieving key templates");
        return Response.ok().entity(BlockConditionDBUtil.getKeyTemplates()).build();
    }
}
