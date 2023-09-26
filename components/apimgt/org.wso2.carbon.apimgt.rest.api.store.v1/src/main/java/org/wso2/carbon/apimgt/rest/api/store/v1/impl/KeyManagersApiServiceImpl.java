package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.dto.KeyManagerPermissionConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIConsumerImpl;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.KeyManagersApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.KeyManagerMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.identity.application.authentication.framework.model.AuthenticatedUser;
import org.wso2.carbon.identity.core.util.IdentityTenantUtil;
import org.wso2.carbon.identity.oauth.OAuthUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.user.core.util.UserCoreUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.wso2.carbon.identity.oauth2.validators.OAuth2TokenValidationMessageContext;
import org.wso2.carbon.apimgt.impl.service.APIKeyMgtRemoteUserStoreMgtService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;

public class KeyManagersApiServiceImpl implements KeyManagersApiService {

    Log log = LogFactory.getLog(KeyManagersApiServiceImpl.class);

    public Response keyManagersGet(String xWSO2Tenant, MessageContext messageContext) {

        String organization = RestApiUtil.getOrganization(messageContext);
        try {
            APIConsumer apiConsumer = new APIConsumerImpl();
            String username = RestApiCommonUtil.getLoggedInUsername();
            List<KeyManagerConfigurationDTO> permittedKeyManagerConfigurations =
                    apiConsumer.getKeyManagerConfigurationsByOrganization(organization, username);
            return Response.ok(KeyManagerMappingUtil.toKeyManagerListDto(permittedKeyManagerConfigurations)).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while retrieving keyManager Details for organization " + organization, log);
        }
        return null;
    }



}
