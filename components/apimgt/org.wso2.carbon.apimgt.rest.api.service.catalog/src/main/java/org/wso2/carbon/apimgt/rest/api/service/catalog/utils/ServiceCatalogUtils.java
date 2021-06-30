/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.service.catalog.utils;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.ServiceEntry;
import org.wso2.carbon.apimgt.impl.ServiceCatalogImpl;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceDTO;

import java.io.ByteArrayInputStream;

/**
 * Util class to handle validations
 */
public class ServiceCatalogUtils {
    private static final ServiceCatalogImpl serviceCatalog = new ServiceCatalogImpl();

    public static boolean checkServiceExistence(String serviceKey, int tenantId) throws APIManagementException {
        return serviceCatalog.getMD5HashByKey(serviceKey, tenantId) != null;
    }

    public static ServiceEntry createServiceFromDTO(ServiceDTO serviceDTO, byte[] definitionFileByteArray) {
        ServiceEntry service = new ServiceEntry();
        service.setName(serviceDTO.getName());
        service.setVersion(serviceDTO.getVersion());
        service.setDescription(serviceDTO.getDescription());
        service.setDefUrl(serviceDTO.getDefinitionUrl());
        service.setServiceUrl(serviceDTO.getServiceUrl());
        service.setDefinitionType(ServiceEntry.DefinitionType.valueOf(serviceDTO.getDefinitionType().value()));
        service.setSecurityType(ServiceEntry.SecurityType.valueOf(serviceDTO.getSecurityType().value()));
        String key = StringUtils.isNotEmpty(serviceDTO.getServiceKey()) ? serviceDTO.getServiceKey() :
                ServiceEntryMappingUtil.generateServiceKey(service);
        service.setKey(key);
        service.setMutualSSLEnabled(serviceDTO.isMutualSSLEnabled());
        service.setEndpointDef(new ByteArrayInputStream(definitionFileByteArray));
        service.setMd5(Md5HashGenerator.calculateMD5Hash(serviceDTO.toString().getBytes()) + Md5HashGenerator
                .calculateMD5Hash(definitionFileByteArray));
        return service;
    }
}
