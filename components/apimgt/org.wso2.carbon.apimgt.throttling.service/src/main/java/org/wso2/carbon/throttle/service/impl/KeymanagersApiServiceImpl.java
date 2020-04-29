package org.wso2.carbon.throttle.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.throttle.service.KeymanagersApiService;
import org.wso2.carbon.throttle.service.dto.KeyManagerDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

public class KeymanagersApiServiceImpl extends KeymanagersApiService {

    Log log = LogFactory.getLog(KeymanagersApiServiceImpl.class);

    @Override
    public Response keymanagersGet() {

        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            Map<String, List<KeyManagerConfigurationDTO>> keyManagerConfigurations =
                    apiAdmin.getAllKeyManagerConfigurations();
            List<KeyManagerDTO> keyManagerDTOList = new ArrayList<>();
            keyManagerConfigurations.forEach((tenantDomain, keyManagerConfigurationDTOList) -> {
                for (KeyManagerConfigurationDTO keyManagerConfigurationDTO : keyManagerConfigurationDTOList) {
                    keyManagerDTOList
                            .add(KeyManagerMappingUtil.toKeyManagerDTO(tenantDomain, keyManagerConfigurationDTO));
                }
            });
            return Response.ok(keyManagerDTOList).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Erorr while retrieving key manager configurations", e, log);
        }
        return null;
    }
}
