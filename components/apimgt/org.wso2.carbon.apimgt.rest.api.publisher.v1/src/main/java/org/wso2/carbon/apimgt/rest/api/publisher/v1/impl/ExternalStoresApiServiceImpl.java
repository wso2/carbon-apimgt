package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.ExternalStoreMappingUtil;

import java.util.Set;

import javax.ws.rs.core.Response;

/**
 * This is the service implementation class for operations related to external stores.
 */
public class ExternalStoresApiServiceImpl implements ExternalStoresApiService {

    /**
     * Get external store list configured for the current of logged in user.
     *
     * @param messageContext CXF Message Context
     * @return External Store list
     */
    @Override
    public Response getAllExternalStores(MessageContext messageContext) throws APIManagementException {
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        Set<APIStore> apiStores = APIUtil.getExternalAPIStores(APIUtil.getTenantIdFromTenantDomain(tenantDomain));
        ExternalStoreListDTO externalStoreListDTO =
                ExternalStoreMappingUtil.fromExternalStoreCollectionToDTO(apiStores);
        return Response.ok().entity(externalStoreListDTO).build();
    }
}
