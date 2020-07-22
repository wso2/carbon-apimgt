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
import org.wso2.carbon.apimgt.internal.service.utils.InternalServiceDataUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;

public class KeymanagersApiServiceImpl implements KeymanagersApiService {

    Log log = LogFactory.getLog(KeymanagersApiServiceImpl.class);

    public Response keymanagersGet(String xWSO2Tenant, MessageContext messageContext) {

        String validatedTenantDomain = InternalServiceDataUtil.validateTenantDomain(xWSO2Tenant);

        if (InternalServiceDataUtil.isUserAuthorizedToTenant(validatedTenantDomain)) {
            try {

                APIAdmin apiAdmin = new APIAdminImpl();
                List<KeyManagerConfigurationDTO> keyManagerConfigurations =
                        apiAdmin.getKeyManagerConfigurationsByTenant(validatedTenantDomain);
                List<KeyManagerDTO> keyManagerDTOList = new ArrayList<>();
                for (KeyManagerConfigurationDTO keyManagerConfiguration : keyManagerConfigurations) {
                    keyManagerDTOList.add(toKeyManagerDTO(validatedTenantDomain, keyManagerConfiguration));
                }
                return Response.ok(keyManagerDTOList).build();
            } catch (APIManagementException e) {
                RestApiUtil.handleInternalServerError("Error while retrieving key manager configurations", e, log);
            }
        }

        return null;
    }

    public static KeyManagerDTO toKeyManagerDTO(String tenantDomain, KeyManagerConfigurationDTO keyManagerConfigurationDTO) {

        KeyManagerDTO keyManagerDTO = new KeyManagerDTO();
        keyManagerDTO.setEnabled(keyManagerConfigurationDTO.isEnabled());
        keyManagerDTO.setName(keyManagerConfigurationDTO.getName());
        keyManagerDTO.setTenantDomain(tenantDomain);
        keyManagerDTO.setType(keyManagerConfigurationDTO.getType());
        keyManagerDTO.setConfiguration(keyManagerConfigurationDTO.getAdditionalProperties());
        return keyManagerDTO;
    }
}
