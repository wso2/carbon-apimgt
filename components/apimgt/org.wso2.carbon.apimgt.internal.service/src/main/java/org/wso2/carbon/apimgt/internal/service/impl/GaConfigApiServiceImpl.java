package org.wso2.carbon.apimgt.internal.service.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.utils.SubscriptionValidationDataUtil;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class GaConfigApiServiceImpl implements GaConfigApiService {

    public Response gaConfigGet(String xWSO2Tenant, String apiId, MessageContext messageContext)
            throws APIManagementException {
        xWSO2Tenant = SubscriptionValidationDataUtil.validateTenantDomain(xWSO2Tenant, messageContext);

        String googleAnalyticsConfiguration = APIUtil.getGAConfigFromRegistry(xWSO2Tenant);

        return Response.ok().entity(googleAnalyticsConfiguration).build();
    }
}
