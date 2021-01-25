package org.wso2.carbon.apimgt.rest.api.service.catalog.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.ServiceEntry;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceInfoDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceMetadataDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.VerifierDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//Add class comments*********
public class ServiceEntryMappingUtil {

    private static final Log log = LogFactory.getLog(Md5HashGenerator.class);

    /**
     * Converts a single metadata file content into a model object
     *
     * @param file Metadata file
     * @return Converted ServiceCatalogInfo model object
     * @throws IOException
     */
    public static ServiceMetadataDTO fromMetadataFileToServiceDTO(File file) throws IOException {

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        ServiceMetadataDTO serviceMetadataDTO = mapper.readValue(file, ServiceMetadataDTO.class);
        return serviceMetadataDTO;
    }

    /**
     * Converts a single metadata file content into a model object
     *
     * @param file Metadata file
     * @return Converted ServiceCatalogInfo model object
     * @throws IOException
     */
    public static ServiceEntry fromFileToServiceCatalogInfo(File file) throws IOException {

        ServiceMetadataDTO serviceMetadataDTO = fromMetadataFileToServiceDTO(file);

        ServiceEntry serviceEntry = new ServiceEntry();
        serviceEntry.setKey(serviceMetadataDTO.getKey());
        serviceEntry.setName(serviceMetadataDTO.getName());
        serviceEntry.setVersion(serviceMetadataDTO.getVersion());
        serviceEntry.setDisplayName(serviceMetadataDTO.getDisplayName());
        serviceEntry.setServiceUrl(serviceMetadataDTO.getServiceUrl());
        serviceEntry.setDefType(serviceMetadataDTO.getDefinitionType().value());
        serviceEntry.setDescription(serviceMetadataDTO.getDescription());
        serviceEntry.setSecurityType(serviceMetadataDTO.getSecurityType().value());
        serviceEntry.setMutualSSLEnabled(serviceMetadataDTO.isMutualSSLEnabled());
        return serviceEntry;
    }

    /**
     * Generate EndPointInfo model
     *
     * @param file Metadata file
     * @param uuid unique id for each entry in service catalog
     * @return Converted ServiceCatalogInfo model object
     * @throws IOException
     */
    public static ServiceEntry generateEndPointInfo(File file, String uuid) throws IOException {

        ServiceEntry serviceEntry = new ServiceEntry();
        InputStream inputFile = new FileInputStream(file);
        serviceEntry.setUuid(uuid);
        serviceEntry.setEndpointDef(inputFile);
        return serviceEntry;
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

    public static HashMap<String, ServiceEntry> fromDirToServiceCatalogInfoMap(String path) {

        // We can use list: then we can go through it and if we need name or something we can just use getters

        HashMap<String, ServiceEntry> endpointDetails = new HashMap<String, ServiceEntry>();
        File[] files = new File(path).listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                ServiceEntry serviceInfo = new ServiceEntry();
                File[] fList = Objects.requireNonNull(file.listFiles());
                String key = null;
                try {
                    for (File aFile : fList) {
                        if (aFile.getName().startsWith(APIConstants.METADATA_FILE_NAME)) {
                            serviceInfo = fromFileToServiceCatalogInfo(aFile);
                            if (!StringUtils.isBlank(serviceInfo.getKey())) {
                                key = serviceInfo.getKey();
                            } else {
                                key = generateServiceKey(serviceInfo);
                            }
                            serviceInfo.setKey(key);
                            serviceInfo.setMetadata(new FileInputStream(aFile));
                        } else { //else if to check oas - check branch there is validation (After M5)
                            serviceInfo.setEndpointDef(new FileInputStream(aFile)); //Closing streams?*******Keep this as ByteStream
                        }
                    }
                } catch (IOException e) {
                    log.error("Failed to fetch metadata information from zip due to " + e.getMessage(), e);
                }
                endpointDetails.put(key, serviceInfo);
            }
        }
        return endpointDetails;
    }

    public static String generateServiceKey(ServiceEntry serviceEntry) {
        String key = serviceEntry.getName() + APIConstants.KEY_SEPARATOR + serviceEntry.getVersion();
        return key.toLowerCase();
    }

//    /**
//     * Converts a single metadata file content into a model object
//     *
//     * @param serviceCatalogInfo ServiceCatalogInfo model object
//     * @return Converted ServiceCRUDStatusDTO DTO object
//     * @throws IOException
//     */
//    public static ServiceCRUDStatusDTO fromServiceCatalogInfoToServiceCRUDStatusDTO(ServiceCatalogInfo serviceCatalogInfo, boolean status) throws IOException {
//        ServiceCRUDStatusDTO serviceCRUDStatusDTO = new ServiceCRUDStatusDTO();
//
//        serviceCRUDStatusDTO.setId(serviceCatalogInfo.getUuid());
//        serviceCRUDStatusDTO.setName(serviceCatalogInfo.getName());
//        serviceCRUDStatusDTO.setDisplayName(serviceCatalogInfo.getDisplayName());
//        serviceCRUDStatusDTO.setVersion(serviceCatalogInfo.getVersion());
//        serviceCRUDStatusDTO.setServiceUrl(serviceCatalogInfo.getServiceUrl());
//        serviceCRUDStatusDTO.setCreatedTime(serviceCatalogInfo.getCreatedTime().toString());
//        serviceCRUDStatusDTO.setLastUpdatedTime(serviceCatalogInfo.getLastUpdatedTime().toString());
//
//        return serviceCRUDStatusDTO;
//    }

    /**
     * Converts a single metadata file content into a model object
     *
     * @param servicesList metadata list of services provided in zip
     * @return build the ServicesStatusListDTO DTO object
     */
    public static ServiceInfoListDTO fromServiceInfoDTOToServiceInfoListDTO(List<ServiceInfoDTO> servicesList) {
        ServiceInfoListDTO serviceInfoListDTO = new ServiceInfoListDTO();

        serviceInfoListDTO.setCount(servicesList.size());
        serviceInfoListDTO.setList(servicesList);

        return serviceInfoListDTO;
    }

    public static List<ServiceInfoDTO> fromServiceCatalogInfoToDTOList(HashMap<String, ServiceEntry> catalogEntries,
                                                                       HashMap<String, List<String>> filteredServices) {
        List<ServiceInfoDTO> serviceStatusList = new ArrayList<>();
        for (String element : filteredServices.get(APIConstants.MAP_KEY_VERIFIED_EXISTING_SERVICE)) { // we can merge these two from utils level
            if (catalogEntries.containsKey(element)) {
                serviceStatusList.add(ServiceEntryMappingUtil.fromServiceCatalogInfoToServiceInfoDTO(catalogEntries.get(element)));
            }
        }
        for (String element : filteredServices.get(APIConstants.MAP_KEY_ACCEPTED_NEW_SERVICE)) {
            if (catalogEntries.containsKey(element)) {
                serviceStatusList.add(ServiceEntryMappingUtil.fromServiceCatalogInfoToServiceInfoDTO(catalogEntries.get(element)));
            }
        }
        return serviceStatusList;

    }

    public static List<ServiceInfoDTO> fromServiceCatalogInfoToDTOList(HashMap<String, ServiceEntry> catalogEntries) {
        List<ServiceInfoDTO> serviceStatusList = new ArrayList<>();
        for (Map.Entry<String, ServiceEntry> entry : catalogEntries.entrySet()) {
            serviceStatusList.add(ServiceEntryMappingUtil.fromServiceCatalogInfoToServiceInfoDTO(catalogEntries.get(entry.getKey())));
        }
        return serviceStatusList;
    }

    /**
     * Converts a single metadata file content into a model object
     *
     * @param serviceEntry ServiceCatalogInfo model object
     * @return Converted ServiceCRUDStatusDTO DTO object
     */
    public static ServiceInfoDTO fromServiceCatalogInfoToServiceInfoDTO(ServiceEntry serviceEntry) {
        ServiceInfoDTO serviceInfoDTO = new ServiceInfoDTO();

        serviceInfoDTO.setId(serviceEntry.getUuid());
        serviceInfoDTO.setName(serviceEntry.getName());
        serviceInfoDTO.setKey(serviceEntry.getKey());
        serviceInfoDTO.setVersion(serviceEntry.getVersion());
        serviceInfoDTO.setMd5(serviceEntry.getMd5());

        return serviceInfoDTO;
    }

    /**
     * Converts a single metadata file content into a model object
     *
     * @param servicesList metadata list of services provided in zip
     * @return build the ServicesStatusListDTO DTO object
     */
    public static ServiceInfoListDTO getServicesResponsePayloadBuilder(List<ServiceInfoDTO> servicesList) {
        ServiceInfoListDTO serviceInfoListDTO = new ServiceInfoListDTO();

        serviceInfoListDTO.setCount(servicesList.size());
        serviceInfoListDTO.setList(servicesList);

        return serviceInfoListDTO;
    }

    /**
     * Converts ServiceCatalogInfo object's input stream entries to files
     *
     * @param serviceEntry metadata list of services provided in zip
     * @return location to the files
     */
    public static String generateServiceFiles(ServiceEntry serviceEntry) {
        String pathToCreateFiles = FileBasedServicesImportExportManager.directoryCreator(RestApiConstants.JAVA_IO_TMPDIR);
        fromInputStreamToFile(serviceEntry.getMetadata(), pathToCreateFiles + File.separator + APIConstants.METADATA_FILE);
        fromInputStreamToFile(serviceEntry.getEndpointDef(), pathToCreateFiles + File.separator + APIConstants.DEFINITION_FILE);

        return pathToCreateFiles;
    }

    private static void fromInputStreamToFile(InputStream inputStream, String outputFile) {
        File file = new File(outputFile);
        try (OutputStream outputStream = new FileOutputStream(file)) {
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            RestApiUtil.handleInternalServerError("Error while preparing resource files before zip", e, log);
        }
    }

    /**
     * Converts JSON String to JSON Object
     *
     * @param jsonInput String json provided in parameter
     * @return list of VerifierDTOs
     */
    public static List<VerifierDTO> fromStringToJSON(String jsonInput) {
        List<VerifierDTO> verifierJSONList;
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            VerifierDTO[] verifierJSONArray = objectMapper.readValue(jsonInput, VerifierDTO[].class);
            verifierJSONList = new ArrayList(Arrays.asList(verifierJSONArray));
            return verifierJSONList;
        } catch (JsonProcessingException e) {
            RestApiUtil.handleInternalServerError("Error while converting verifier JSON String to JSON object", e, log);
        }
        return null;
    }
}
