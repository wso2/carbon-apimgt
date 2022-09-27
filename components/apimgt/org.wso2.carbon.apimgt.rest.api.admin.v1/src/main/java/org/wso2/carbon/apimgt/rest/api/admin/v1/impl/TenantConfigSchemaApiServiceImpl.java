package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.rest.api.admin.v1.TenantConfigSchemaApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.impl.TenantConfigSchemaCommonImpl;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import javax.ws.rs.core.Response;

public class TenantConfigSchemaApiServiceImpl implements TenantConfigSchemaApiService {

    @Override
    public Response exportTenantConfigSchema(MessageContext messageContext) {
        String tenantConfig = TenantConfigSchemaCommonImpl.exportTenantConfigSchema();
        return Response.ok().entity(tenantConfig)
                .header(RestApiConstants.HEADER_CONTENT_TYPE, RestApiConstants.APPLICATION_JSON).build();
    }
}
