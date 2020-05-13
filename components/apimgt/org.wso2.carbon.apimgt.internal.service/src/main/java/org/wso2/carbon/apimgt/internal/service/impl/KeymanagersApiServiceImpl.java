package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.KeyManagerDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.List;

import java.io.InputStream;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class KeymanagersApiServiceImpl implements KeymanagersApiService {
    Log log = LogFactory.getLog(KeymanagersApiServiceImpl.class);

    public Response keymanagersGet(MessageContext messageContext) {
        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            Map<String, List<KeyManagerConfigurationDTO>> keyManagerConfigurations =
                    apiAdmin.getAllKeyManagerConfigurations();
            List<KeyManagerDTO> keyManagerDTOList = new ArrayList<>();
            keyManagerConfigurations.forEach((tenantDomain, keyManagerConfigurationDTOList) -> {
                for (KeyManagerConfigurationDTO keyManagerConfigurationDTO : keyManagerConfigurationDTOList) {
                    keyManagerDTOList
                            .add(toKeyManagerDTO(tenantDomain, keyManagerConfigurationDTO));
                }
            });
            return Response.ok(keyManagerDTOList).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Erorr while retrieving key manager configurations", e, log);
        }
        return null;
    }

    public static KeyManagerDTO toKeyManagerDTO(String tenantDomain,KeyManagerConfigurationDTO keyManagerConfigurationDTO){
        KeyManagerDTO keyManagerDTO = new KeyManagerDTO();
        keyManagerDTO.setEnabled(keyManagerConfigurationDTO.isEnabled());
        keyManagerDTO.setName(keyManagerConfigurationDTO.getName());
        keyManagerDTO.setTenantDomain(tenantDomain);
        keyManagerDTO.setType(keyManagerConfigurationDTO.getType());
        keyManagerDTO.setConfiguration(keyManagerConfigurationDTO.getAdditionalProperties());
        return keyManagerDTO;
    }
}
