package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIStore;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ExternalStoreDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.ExternalStoreMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.List;

import java.io.InputStream;
import java.util.Set;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * This is the service implementation class for operations related to external stores.
 */
public class ExternalStoresApiServiceImpl implements ExternalStoresApiService {

    private static final Log log = LogFactory.getLog(ExternalStoresApiServiceImpl.class);

    /**
     * Get external store list configured for the current of logged in user.
     *
     * @param messageContext CXF Message Context
     * @return External Store list
     */
    public Response externalStoresGet(MessageContext messageContext) {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        try {
            Set<APIStore> apiStores = APIUtil.getExternalAPIStores(APIUtil.getTenantIdFromTenantDomain(tenantDomain));
            ExternalStoreListDTO externalStoreListDTO =
                    ExternalStoreMappingUtil.fromExternalStoreCollectionToDTO(apiStores);
            return Response.ok().entity(externalStoreListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving external API Stores for tenant domain:" + tenantDomain;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
