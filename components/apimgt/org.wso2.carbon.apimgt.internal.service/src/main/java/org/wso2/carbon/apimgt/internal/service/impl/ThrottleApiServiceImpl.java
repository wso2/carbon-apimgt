package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.internal.service.ThrottleApiService;

import javax.ws.rs.core.Response;

public class ThrottleApiServiceImpl implements ThrottleApiService {

    @Override
    public Response throttleGet(String query, MessageContext messageContext) throws APIManagementException {
        return Response.ok().entity(ThrottlingDBUtil.getThrottledEvents(query)).build();
    }
}
