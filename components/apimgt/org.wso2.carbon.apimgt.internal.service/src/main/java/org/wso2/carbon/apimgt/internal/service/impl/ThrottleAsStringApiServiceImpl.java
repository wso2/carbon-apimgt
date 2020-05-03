package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.internal.service.ThrottleAsStringApiService;

import javax.ws.rs.core.Response;

public class ThrottleAsStringApiServiceImpl implements ThrottleAsStringApiService {
    @Override
    public Response throttleAsStringGet(String query, MessageContext messageContext) throws APIManagementException {
        return Response.ok().entity(ThrottlingDBUtil.getThrottledEventsAsString(query)).build();
    }
}
