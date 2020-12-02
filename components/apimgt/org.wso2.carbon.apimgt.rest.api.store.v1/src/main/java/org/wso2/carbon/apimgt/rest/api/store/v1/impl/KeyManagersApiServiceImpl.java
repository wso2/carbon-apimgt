package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.KeyManagersApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.KeyManagerMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.List;

import javax.ws.rs.core.Response;

public class KeyManagersApiServiceImpl implements KeyManagersApiService {

    Log log = LogFactory.getLog(KeyManagersApiServiceImpl.class);

    public Response keyManagersGet(String xWSO2Tenant, MessageContext messageContext) {

        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            List<KeyManagerConfigurationDTO> keyManagerConfigurations =
                    apiAdmin.getKeyManagerConfigurationsByTenant(tenantDomain);
            for (KeyManagerConfigurationDTO keyManagerConfiguration : keyManagerConfigurations) {
                if (APIConstants.KeyManager.DEFAULT_KEY_MANAGER.equals(keyManagerConfiguration.getName())) {
                    APIUtil.setTokenAndRevokeEndpointsToDevPortal(keyManagerConfiguration);
                }
            }
            return Response.ok(KeyManagerMappingUtil.toKeyManagerListDto(keyManagerConfigurations)).build();
        } catch (APIManagementException e) {
            RestApiUtil
                    .handleInternalServerError("Error while retrieving keyManager Details for Tenant " + tenantDomain,
                            log);
        }
        return null;
    }
}
