package org.wso2.carbon.apimgt.rest.api.service.catalog.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.wso2.carbon.apimgt.api.model.EndPointInfo;
import org.wso2.carbon.apimgt.api.model.ServiceCatalogInfo;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceDTO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;

public class MetadataMappingUtil {

    /**
     * Converts a single metadata file content into a model object
     *
     * @param file Metadata file
     * @return Converted ServiceCatalogInfo model object
     * @throws IOException
     */
    public static ServiceDTO fromMetadataFileToServiceDTO(File file) throws IOException {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ServiceDTO serviceDTO = mapper.readValue(file, ServiceDTO.class);
        return serviceDTO;
    }

    /**
     * Converts a single metadata file content into a model object
     *
     * @param file Metadata file
     * @param md5 md5 hash value
     * @return Converted ServiceCatalogInfo model object
     * @throws IOException
     */
    public static ServiceCatalogInfo fromServiceDTOToServiceCatalogInfo(File file, String md5) throws IOException {

        ServiceDTO serviceDTO = fromMetadataFileToServiceDTO(file);

        ServiceCatalogInfo serviceCatalogInfo = new ServiceCatalogInfo();
        serviceCatalogInfo.setMd5(md5);
        serviceCatalogInfo.setName(serviceDTO.getName());
        serviceCatalogInfo.setVersion(serviceDTO.getVersion());
        serviceCatalogInfo.setDisplayName(serviceDTO.getDisplayName());
        serviceCatalogInfo.setServiceUrl(serviceDTO.getServiceUrl());
        serviceCatalogInfo.setDefType(serviceDTO.getDefinitionType().value());
        serviceCatalogInfo.setDescription(serviceDTO.getDescription());
        serviceCatalogInfo.setSecurityType(serviceDTO.getSecurityType().value());
        serviceCatalogInfo.setIsMutualSSLEnabled(serviceDTO.isMutualSSLEnabled());
        serviceCatalogInfo.setCreatedTime(Timestamp.valueOf(serviceDTO.getCreatedTime()));
        return serviceCatalogInfo;
    }

    /**
     * Generate EndPointInfo model
     *
     * @param file Metadata file
     * @param uuid unique id for each entry in service catalog
     * @return Converted ServiceCatalogInfo model object
     * @throws IOException
     */
    public static EndPointInfo generateEndPointInfo(File file, String uuid) throws IOException {

        EndPointInfo endPointInfo = new EndPointInfo();
        InputStream inputFile = new FileInputStream(file);
        endPointInfo.setUuid(uuid);
        endPointInfo.setEndPointDef(inputFile);
        return endPointInfo;
    }
}
