package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConsumerImpl;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.KeyManagersApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.KeyManagerMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.List;

import javax.ws.rs.core.Response;

public class KeyManagersApiServiceImpl implements KeyManagersApiService {

    Log log = LogFactory.getLog(KeyManagersApiServiceImpl.class);

    public Response keyManagersGet(String xWSO2Tenant, MessageContext messageContext) {

        String organization = RestApiUtil.getOrganization(messageContext);
        try {
            APIAdmin apiAdmin = new APIAdminImpl();
            APIConsumer apiConsumer = new APIConsumerImpl();
            String username = RestApiCommonUtil.getLoggedInUsername();
            List<KeyManagerConfigurationDTO> permittedKeyManagerConfigurations =
                    apiConsumer.getKeyManagerConfigurationsByOrganization(organization, username);
            List<KeyManagerConfigurationDTO> globalKeyManagerConfigurations
                    = apiAdmin.getGlobalKeyManagerConfigurations();
            permittedKeyManagerConfigurations.addAll(globalKeyManagerConfigurations);
            return Response.ok(KeyManagerMappingUtil.toKeyManagerListDto(permittedKeyManagerConfigurations)).build();

        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while retrieving keyManager Details for organization " + organization, log);
        }
        return null;
    }
}
