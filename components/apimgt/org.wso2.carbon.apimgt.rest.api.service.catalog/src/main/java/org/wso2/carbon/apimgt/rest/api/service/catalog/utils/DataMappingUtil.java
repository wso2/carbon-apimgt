package org.wso2.carbon.apimgt.rest.api.service.catalog.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.EndPointInfo;
import org.wso2.carbon.apimgt.api.model.ServiceCatalogEntry;
import org.wso2.carbon.apimgt.api.model.ServiceCatalogInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.*;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

    public static String keyGenerator(ServiceCatalogInfo serviceCatalogInfo) {
        String key = serviceCatalogInfo.getName() + APIConstants.KEY_SEPARATOR + serviceCatalogInfo.getVersion();
        return key.toLowerCase();
    }

    /**
     * Converts a single metadata file content into a model object
     *
     * @param serviceCatalogInfo ServiceCatalogInfo model object
     * @return Converted ServiceCRUDStatusDTO DTO object
     * @throws IOException
     */
    public static ServiceCRUDStatusDTO fromServiceCatalogInfoToServiceCRUDStatusDTO(ServiceCatalogInfo serviceCatalogInfo, boolean status) throws IOException {
        ServiceCRUDStatusDTO serviceCRUDStatusDTO = new ServiceCRUDStatusDTO();

        serviceCRUDStatusDTO.setId(serviceCatalogInfo.getUuid());
        serviceCRUDStatusDTO.setName(serviceCatalogInfo.getName());
        serviceCRUDStatusDTO.setDisplayName(serviceCatalogInfo.getDisplayName());
        serviceCRUDStatusDTO.setDescription(serviceCatalogInfo.getDescription());
        serviceCRUDStatusDTO.setVersion(serviceCatalogInfo.getVersion());
        serviceCRUDStatusDTO.setServiceUrl(serviceCatalogInfo.getServiceUrl());
        serviceCRUDStatusDTO.setDefinitionType(ServiceCRUDStatusDTO.DefinitionTypeEnum.fromValue(serviceCatalogInfo.getDefType()));
        serviceCRUDStatusDTO.setSecurityType(ServiceCRUDStatusDTO.SecurityTypeEnum.fromValue(serviceCatalogInfo.getSecurityType()));
        serviceCRUDStatusDTO.setMutualSSLEnabled(serviceCatalogInfo.isMutualSSLEnabled());
        serviceCRUDStatusDTO.setCreatedTime(serviceCatalogInfo.getCreatedTime().toString());
        serviceCRUDStatusDTO.setLastUpdatedTime(serviceCatalogInfo.getLastUpdatedTime().toString());
        serviceCRUDStatusDTO.setCatalogUpdated(status);

        return serviceCRUDStatusDTO;
    }

    /**
     * Converts a single metadata file content into a model object
     *
     * @param servicesList metadata list of services provided in zip
     * @param paginationDTO Pagination data
     * @return build the ServicesStatusListDTO DTO object
     */
    public static ServicesStatusListDTO responsePayloadBuilder(List<ServiceCRUDStatusDTO> servicesList, PaginationDTO paginationDTO) {
        ServicesStatusListDTO servicesStatusListDTO = new ServicesStatusListDTO();

        servicesStatusListDTO.setCount(servicesList.size());
        servicesStatusListDTO.setList(servicesList);
        servicesStatusListDTO.setPagination(paginationDTO);

        return servicesStatusListDTO;
    }

    public static List<ServiceCRUDStatusDTO> responsePayloadListBuilder(HashMap<String, ServiceCatalogEntry> catalogEntries,
                                                                        HashMap<String, List<String>> existingServices,
                                                                        HashMap<String, List<String>> newServices){
        List<ServiceCRUDStatusDTO> serviceStatusList = new ArrayList<>();
        for (String element : existingServices.get(APIConstants.MAP_KEY_VERIFIED)) {
            if (catalogEntries.containsKey(element)) {
                try {
                    serviceStatusList.add(DataMappingUtil.fromServiceCatalogInfoToServiceCRUDStatusDTO(catalogEntries.get(element).getServiceCatalogInfo(), true));
                } catch (IOException e) {
                    RestApiUtil.handleInternalServerError("Error while forming response dto", e, log);
                }
            }
        }
        for (String element : existingServices.get(APIConstants.MAP_KEY_NOT_CHANGED)) {
            if (catalogEntries.containsKey(element)) {
                try {
                    serviceStatusList.add(DataMappingUtil.fromServiceCatalogInfoToServiceCRUDStatusDTO(catalogEntries.get(element).getServiceCatalogInfo(), true));
                } catch (IOException e) {
                    RestApiUtil.handleInternalServerError("Error while forming response dto", e, log);
                }
            }
        }
        for (String element : existingServices.get(APIConstants.MAP_KEY_IGNORED)) {
            if (catalogEntries.containsKey(element)) {
                try {
                    serviceStatusList.add(DataMappingUtil.fromServiceCatalogInfoToServiceCRUDStatusDTO(catalogEntries.get(element).getServiceCatalogInfo(), true));
                } catch (IOException e) {
                    RestApiUtil.handleInternalServerError("Error while forming response dto", e, log);
                }
            }
        }
        for (String element : newServices.get(APIConstants.MAP_KEY_ACCEPTED)) {
            if (catalogEntries.containsKey(element)) {
                try {
                    serviceStatusList.add(DataMappingUtil.fromServiceCatalogInfoToServiceCRUDStatusDTO(catalogEntries.get(element).getServiceCatalogInfo(), true));
                } catch (IOException e) {
                    RestApiUtil.handleInternalServerError("Error while forming response dto", e, log);
                }
            }
        }
        for (String element : newServices.get(APIConstants.MAP_KEY_IGNORED)) {
            if (catalogEntries.containsKey(element)) {
                try {
                    serviceStatusList.add(DataMappingUtil.fromServiceCatalogInfoToServiceCRUDStatusDTO(catalogEntries.get(element).getServiceCatalogInfo(), true));
                } catch (IOException e) {
                    RestApiUtil.handleInternalServerError("Error while forming response dto", e, log);
                }
            }
        }
        return serviceStatusList;
    }

    /**
     * Converts a single metadata file content into a model object
     *
     * @param serviceCatalogInfo ServiceCatalogInfo model object
     * @return Converted ServiceCRUDStatusDTO DTO object
     */
    public static ServiceInfoDTO fromServiceCatalogInfoToServiceInfoDTO(ServiceCatalogInfo serviceCatalogInfo) {
        ServiceInfoDTO serviceInfoDTO = new ServiceInfoDTO();

        serviceInfoDTO.setId(serviceCatalogInfo.getUuid());
        serviceInfoDTO.setName(serviceCatalogInfo.getName());
        serviceInfoDTO.setKey(serviceCatalogInfo.getKey());
        serviceInfoDTO.setVersion(serviceCatalogInfo.getVersion());
        serviceInfoDTO.setMd5(serviceCatalogInfo.getMd5());

        return serviceInfoDTO;
    }

    /**
     * Converts a single metadata file content into a model object
     *
     * @param servicesList metadata list of services provided in zip
     * @param paginationDTO Pagination data
     * @return build the ServicesStatusListDTO DTO object
     */
    public static ServicesListDTO StatusResponsePayloadBuilder(List<ServiceInfoDTO> servicesList, PaginationDTO paginationDTO) {
        ServicesListDTO servicesListDTO = new ServicesListDTO();

        servicesListDTO.setCount(servicesList.size());
        servicesListDTO.setList(servicesList);
        servicesListDTO.setPagination(paginationDTO);

        return servicesListDTO;
    }

    /**
     * Converts EndPointInfo object's input stream entries to files
     *
     * @param endPointInfo metadata list of services provided in zip
     *
     * @return location to the files
     */
    public static String fromEndPointInfoToFiles(EndPointInfo endPointInfo) {
        String pathToCreateFiles = FileBasedServicesImportExportManager.directoryCreator(RestApiConstants.JAVA_IO_TMPDIR);
        fromInputStreamToFile(endPointInfo.getMetadata(), pathToCreateFiles + File.separator + APIConstants.METADATA_FILE);
        fromInputStreamToFile(endPointInfo.getEndPointDef(), pathToCreateFiles + File.separator + APIConstants.DEFINITION_FILE);

        return pathToCreateFiles;
    }

    private static void fromInputStreamToFile(InputStream inputStream, String outputFile) {
        File file = new File(outputFile);
        try(OutputStream outputStream = new FileOutputStream(file)){
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            RestApiUtil.handleInternalServerError("Error while preparing resource files before zip", e, log);
        }
    }
}
