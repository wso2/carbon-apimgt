package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ExternalStoresApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.impl.ExternalStoresApiCommonImpl;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ExternalStoreListDTO;

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

        ExternalStoreListDTO externalStoreListDTO = ExternalStoresApiCommonImpl.getAllExternalStores();
        return Response.ok().entity(externalStoreListDTO).build();
    }
}
