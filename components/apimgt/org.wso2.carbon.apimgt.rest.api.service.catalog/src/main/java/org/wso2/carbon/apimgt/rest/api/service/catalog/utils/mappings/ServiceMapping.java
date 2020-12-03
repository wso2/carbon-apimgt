package org.wso2.carbon.apimgt.rest.api.service.catalog.utils.mappings;

import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.model.Service;

public class ServiceMapping {
    public static ServiceDTO fromServiceToDTO(Service service){
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setId(service.getId());
        serviceDTO.setName(service.getName());
        serviceDTO.setDisplayName(service.getDisplayName());
        serviceDTO.description(service.getDescription());
        serviceDTO.setVersion(service.getVersion());;
        serviceDTO.setServiceUrl(service.getServiceUrl());
        return serviceDTO;
    }
}
