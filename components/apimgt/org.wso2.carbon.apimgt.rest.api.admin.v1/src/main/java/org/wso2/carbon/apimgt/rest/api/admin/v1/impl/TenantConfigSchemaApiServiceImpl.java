package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import javax.ws.rs.core.Response;

public class TenantConfigSchemaApiServiceImpl implements TenantConfigSchemaApiService {

    public Response exportTenantConfigSchema(MessageContext messageContext) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        String tenantConfig = apiAdmin.getTenantConfigSchema(RestApiCommonUtil.getLoggedInUserTenantDomain());
        return Response.ok().entity(tenantConfig)
                .header(RestApiConstants.HEADER_CONTENT_TYPE, RestApiConstants.APPLICATION_JSON).build();
    }
}
