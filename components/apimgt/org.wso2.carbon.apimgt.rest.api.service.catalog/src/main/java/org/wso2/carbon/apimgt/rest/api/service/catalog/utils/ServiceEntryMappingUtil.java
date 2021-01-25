package org.wso2.carbon.apimgt.rest.api.service.catalog.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.ServiceCatalogInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.*;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.*;
import java.util.*;

public class DataMappingUtil {

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
    public static ServiceCatalogInfo fromFileToServiceCatalogInfo(File file) throws IOException {

        ServiceMetadataDTO serviceMetadataDTO = fromMetadataFileToServiceDTO(file);

        ServiceCatalogInfo serviceCatalogInfo = new ServiceCatalogInfo();
        serviceCatalogInfo.setKey(serviceMetadataDTO.getKey());
        serviceCatalogInfo.setName(serviceMetadataDTO.getName());
        serviceCatalogInfo.setVersion(serviceMetadataDTO.getVersion());
        serviceCatalogInfo.setDisplayName(serviceMetadataDTO.getDisplayName());
        serviceCatalogInfo.setServiceUrl(serviceMetadataDTO.getServiceUrl());
        serviceCatalogInfo.setDefType(serviceMetadataDTO.getDefinitionType().value());
        serviceCatalogInfo.setDescription(serviceMetadataDTO.getDescription());
        serviceCatalogInfo.setSecurityType(serviceMetadataDTO.getSecurityType().value());
        serviceCatalogInfo.setMutualSSLEnabled(serviceMetadataDTO.isMutualSSLEnabled());
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
    public static ServiceCatalogInfo generateEndPointInfo(File file, String uuid) throws IOException {

        ServiceCatalogInfo serviceCatalogInfo = new ServiceCatalogInfo();
        InputStream inputFile = new FileInputStream(file);
        serviceCatalogInfo.setUuid(uuid);
        serviceCatalogInfo.setEndpointDef(inputFile);
        return serviceCatalogInfo;
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

    public static HashMap<String, ServiceCatalogInfo> fromDirToServiceCatalogInfoMap(String path) {

        // We can use list: then we can go through it and if we need name or something we can just use getters

        HashMap<String, ServiceCatalogInfo> endpointDetails = new HashMap<String, ServiceCatalogInfo>();
        File[] files = new File(path).listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                ServiceCatalogInfo serviceInfo = new ServiceCatalogInfo();
                File[] fList = Objects.requireNonNull(file.listFiles());
                String key = null;
                try {
                    for (File aFile : fList) {
                        if (aFile.getName().startsWith(APIConstants.METADATA_FILE_NAME)) {
                            serviceInfo = fromFileToServiceCatalogInfo(aFile);
                            if (!StringUtils.isBlank(serviceInfo.getKey())) {
                                key = serviceInfo.getKey();
                            } else {
                                key = keyGenerator(serviceInfo);
                            }
                            serviceInfo.setKey(key);
                            serviceInfo.setMetadata(new FileInputStream(aFile));
                        } else {
                            serviceInfo.setEndpointDef(new FileInputStream(aFile));
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
        serviceCRUDStatusDTO.setVersion(serviceCatalogInfo.getVersion());
        serviceCRUDStatusDTO.setServiceUrl(serviceCatalogInfo.getServiceUrl());
        serviceCRUDStatusDTO.setCreatedTime(serviceCatalogInfo.getCreatedTime().toString());
        serviceCRUDStatusDTO.setLastUpdatedTime(serviceCatalogInfo.getLastUpdatedTime().toString());

        return serviceCRUDStatusDTO;
    }

    /**
     * Converts a single metadata file content into a model object
     *
     * @param servicesList metadata list of services provided in zip
     * @return build the ServicesStatusListDTO DTO object
     */
    public static ServiceInfoListDTO responsePayloadBuilder(List<ServiceInfoDTO> servicesList) {
        ServiceInfoListDTO serviceInfoListDTO = new ServiceInfoListDTO();

        serviceInfoListDTO.setCount(servicesList.size());
        serviceInfoListDTO.setList(servicesList);

        return serviceInfoListDTO;
    }

    public static List<ServiceInfoDTO> responsePayloadListBuilder(HashMap<String, ServiceCatalogInfo> catalogEntries,
                                                                        HashMap<String, List<String>> filteredServices){
        List<ServiceInfoDTO> serviceStatusList = new ArrayList<>();
        for (String element : filteredServices.get(APIConstants.MAP_KEY_VERIFIED_EXISTING_SERVICE)) {
            if (catalogEntries.containsKey(element)) {
                serviceStatusList.add(DataMappingUtil.fromServiceCatalogInfoToServiceInfoDTO(catalogEntries.get(element)));
            }
        }
        for (String element : filteredServices.get(APIConstants.MAP_KEY_ACCEPTED_NEW_SERVICE)) {
            if (catalogEntries.containsKey(element)) {
                serviceStatusList.add(DataMappingUtil.fromServiceCatalogInfoToServiceInfoDTO(catalogEntries.get(element)));
            }
        }
        return serviceStatusList;
    }

    public static List<ServiceInfoDTO> updateResponsePayloadListBuilder(HashMap<String, ServiceCatalogInfo> catalogEntries){
        List<ServiceInfoDTO> serviceStatusList = new ArrayList<>();
        for (Map.Entry<String,ServiceCatalogInfo> entry : catalogEntries.entrySet()) {
            serviceStatusList.add(DataMappingUtil.fromServiceCatalogInfoToServiceInfoDTO(catalogEntries.get(entry.getKey())));
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
     * @param serviceCatalogInfo metadata list of services provided in zip
     *
     * @return location to the files
     */
    public static String filesGenerator(ServiceCatalogInfo serviceCatalogInfo) {
        String pathToCreateFiles = FileBasedServicesImportExportManager.directoryCreator(RestApiConstants.JAVA_IO_TMPDIR);
        fromInputStreamToFile(serviceCatalogInfo.getMetadata(), pathToCreateFiles + File.separator + APIConstants.METADATA_FILE);
        fromInputStreamToFile(serviceCatalogInfo.getEndpointDef(), pathToCreateFiles + File.separator + APIConstants.DEFINITION_FILE);

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

    /**
     * Converts JSON String to JSON Object
     *
     * @param jsonInput String json provided in parameter
     *
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
