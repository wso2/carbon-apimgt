package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.internal.service.KeymanagersApiService;
import org.wso2.carbon.apimgt.internal.service.dto.KeyManagerDTO;
import org.wso2.carbon.apimgt.internal.service.utils.SubscriptionValidationDataUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

/**
 * This Class contains KeyManagerInternal Data Service Implementations.
 */
public class KeymanagersApiServiceImpl implements KeymanagersApiService {

    Log log = LogFactory.getLog(KeymanagersApiServiceImpl.class);

    public static KeyManagerDTO toKeyManagerDTO(KeyManagerConfigurationDTO keyManagerConfigurationDTO) {

        KeyManagerDTO keyManagerDTO = new KeyManagerDTO();
        keyManagerDTO.setUuid(keyManagerConfigurationDTO.getUuid());
        keyManagerDTO.setEnabled(keyManagerConfigurationDTO.isEnabled());
        keyManagerDTO.setName(keyManagerConfigurationDTO.getName());
        keyManagerDTO.setOrganization(keyManagerConfigurationDTO.getOrganization());
        keyManagerDTO.setType(keyManagerConfigurationDTO.getType());
        keyManagerDTO.setTokenType(KeyManagerDTO.TokenTypeEnum.fromValue(keyManagerConfigurationDTO.getTokenType()));
        keyManagerDTO.setAdditionalProperties(keyManagerConfigurationDTO.getAdditionalProperties());
        return keyManagerDTO;
    }

    public Response keymanagersGet(String xWSO2Tenant, MessageContext messageContext) {

        xWSO2Tenant = SubscriptionValidationDataUtil.validateTenantDomain(xWSO2Tenant, messageContext);
        String authenticatedTenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();

        try {

            APIAdmin apiAdmin = new APIAdminImpl();
            List<KeyManagerConfigurationDTO> keyManagerConfigurations =
                    apiAdmin.getKeyManagerConfigurationsByOrganization(xWSO2Tenant, false);
            List<KeyManagerConfigurationDTO> globalKeyManagerConfigurations = apiAdmin
                    .getGlobalKeyManagerConfigurations();
            keyManagerConfigurations.addAll(globalKeyManagerConfigurations);
            List<KeyManagerDTO> keyManagerDTOList = new ArrayList<>();
            for (KeyManagerConfigurationDTO keyManagerConfiguration : keyManagerConfigurations) {
                keyManagerDTOList.add(toKeyManagerDTO(keyManagerConfiguration));
            }
            return Response.ok(redactAdditionalPropertiesForNonSuperTenant(
                    keyManagerDTOList, authenticatedTenantDomain)).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving key manager configurations", e, log);
        }
        return null;
    }

    /**
     * The key manager list above includes the credential material (Username/Password, OAuth client
     * secret, etc.) carried in each key manager's additionalProperties, decrypted. This is served to
     * every caller that clears the WAR's own permission gate, including an ordinary tenant
     * administrator, and always includes the global key managers regardless of which organization
     * the caller belongs to. A non-super caller must not receive that credential material — for its
     * own key managers it already has the admin REST API, and for the global ones it has no
     * legitimate reason to read them at all. An unresolved authenticated tenant is treated as
     * non-super (fail closed).
     */
    static List<KeyManagerDTO> redactAdditionalPropertiesForNonSuperTenant(
            List<KeyManagerDTO> keyManagerDTOList, String authenticatedTenantDomain) {

        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(authenticatedTenantDomain)) {
            for (KeyManagerDTO keyManagerDTO : keyManagerDTOList) {
                keyManagerDTO.setAdditionalProperties(null);
            }
        }
        return keyManagerDTOList;
    }
}
