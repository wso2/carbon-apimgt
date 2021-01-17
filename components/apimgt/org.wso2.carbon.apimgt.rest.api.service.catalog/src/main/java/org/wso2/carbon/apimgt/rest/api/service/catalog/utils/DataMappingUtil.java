package org.wso2.carbon.apimgt.rest.api.service.catalog.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.EndPointInfo;
import org.wso2.carbon.apimgt.api.model.ServiceCatalogEntry;
import org.wso2.carbon.apimgt.api.model.ServiceCatalogInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceDTO;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Objects;

public class DataMappingUtil {

    private static final Log log = LogFactory.getLog(Md5HashGenerator.class);

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
     * @return Converted ServiceCatalogInfo model object
     * @throws IOException
     */
    public static ServiceCatalogInfo fromServiceDTOToServiceCatalogInfo(File file) throws IOException {

        ServiceDTO serviceDTO = fromMetadataFileToServiceDTO(file);

        ServiceCatalogInfo serviceCatalogInfo = new ServiceCatalogInfo();
        serviceCatalogInfo.setName(serviceDTO.getName());
        serviceCatalogInfo.setVersion(serviceDTO.getVersion());
        serviceCatalogInfo.setDisplayName(serviceDTO.getDisplayName());
        serviceCatalogInfo.setServiceUrl(serviceDTO.getServiceUrl());
        serviceCatalogInfo.setDefType(serviceDTO.getDefinitionType().value());
        serviceCatalogInfo.setDescription(serviceDTO.getDescription());
        serviceCatalogInfo.setSecurityType(serviceDTO.getSecurityType().value());
        serviceCatalogInfo.setMutualSSLEnabled(serviceDTO.isMutualSSLEnabled());
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

    public static int dirCount(String path) {
        File[] files = new File(path).listFiles();
        int count = 0;
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                count++;
            }
        }
        return count;
    }

    public static HashMap<String, ServiceCatalogEntry> fromDirToServiceCatalogEntryMap(String path) {

        // We can use list: then we can go through it and if we need name or something we can just use getters

        HashMap<String, ServiceCatalogEntry> endpointDetails = new HashMap<String, ServiceCatalogEntry>();
        File[] files = new File(path).listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                ServiceCatalogEntry serviceCatalogEntry = new ServiceCatalogEntry();
                EndPointInfo endPointInfo = new EndPointInfo();
                ServiceCatalogInfo serviceInfo;
                File[] fList = Objects.requireNonNull(file.listFiles());
                String key = null;
                try {
                    for (File aFile : fList) {
                        if (aFile.getName().startsWith(APIConstants.METADATA_FILE_NAME)) {
                            serviceInfo = fromServiceDTOToServiceCatalogInfo(aFile);
                            serviceCatalogEntry.setServiceCatalogInfo(serviceInfo);
                            key = keyGenerator(serviceInfo);
                            serviceInfo.setKey(key);
                            endPointInfo.setMetadata(new FileInputStream(aFile));
                        } else {
                            endPointInfo.setEndPointDef(new FileInputStream(aFile));
                        }
                    }
                } catch (IOException e) {
                    log.error("Failed to fetch metadata information from zip due to " + e.getMessage(), e);
                }
                serviceCatalogEntry.setEndPointInfo(endPointInfo);
                endpointDetails.put(key, serviceCatalogEntry);
            }
        }
        return endpointDetails;
    }

    public static String keyGenerator(ServiceCatalogInfo serviceCatalogInfo){
        String key =  serviceCatalogInfo.getName() + APIConstants.KEY_SEPARATOR + serviceCatalogInfo.getVersion();
        return key.toLowerCase();
    }
}
