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

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.ServiceEntry;
import org.wso2.carbon.apimgt.api.model.ServiceFilterParams;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceInfoDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceInfoListDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceListDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Mapping class for Service Catalog services
 */
public class ServiceEntryMappingUtil {

    private static final Log log = LogFactory.getLog(ServiceEntryMappingUtil.class);

    /**
     * Converts a single metadata file content into a ServiceEntry model
     *
     * @param file Metadata file
     * @return Converted ServiceEntry model object
     * @throws IOException
     */
    static ServiceEntry fromFileToServiceEntry(File file, ServiceEntry service) throws IOException {
        if (service == null) {
            service = new ServiceEntry();
        }
        try {
            ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            service = mapper.readValue(file, ServiceEntry.class);
            if (StringUtils.isBlank(service.getKey())) {
                service.setKey(generateServiceKey(service));
            }
        } catch (InvalidFormatException e) {
            RestApiUtil.handleBadRequest("One or more parameters contain disallowed values", e, log);
        }
        return service;
    }

    /**
     * Generate Hash Map to hold all the ServiceEntry objects relevant to the services included in zip
     *
     * @param path path to the directory which include files
     * @return HashMap with service key as key and ServiceEntry object as value
     */
    public static HashMap<String, ServiceEntry> fromDirToServiceEntryMap(String path) {
        HashMap<String, ServiceEntry> endpointDetails = new HashMap<>();
        File[] files = new File(path).listFiles();
        assert files != null;
        for (File file : files) {
            if (file.isDirectory()) {
                ServiceEntry serviceInfo = new ServiceEntry();
                File metadataFile = getServiceFile(APIConstants.METADATA_FILE, file);
                File definitionFile;
                if (file.getName().contains(APIConstants.PROXY_SERVICE_NAME_SUFFIX)
                        && new File(file.getAbsoluteFile(), APIConstants.DEFINITION_WSDL_FILE).exists()) {
                    definitionFile = getServiceFile(APIConstants.DEFINITION_WSDL_FILE, file);
                } else {
                    definitionFile = getServiceFile(APIConstants.DEFINITION_FILE, file);
                }
                String key = null;
                try {
                    serviceInfo = fromFileToServiceEntry(metadataFile, serviceInfo);
                    serviceInfo.setMetadata(new ByteArrayInputStream(FileUtils.readFileToByteArray(metadataFile)));
                    key = serviceInfo.getKey();
                    serviceInfo.setEndpointDef(new ByteArrayInputStream(FileUtils.readFileToByteArray(definitionFile)));
                } catch (IOException e) {
                    RestApiUtil.handleInternalServerError("Error while reading service resource files. " +
                            "Zip might not include valid data", e, log);
                }
                endpointDetails.put(key, serviceInfo);
            }
        }
        return endpointDetails;
    }

    private static File getServiceFile(String fileName, File files) {
        FilenameFilter filenameFilter = (file, name) -> name.equals(fileName);
        File[] retrievedFiles = files.listFiles(filenameFilter);
        if (retrievedFiles == null || retrievedFiles.length == 0) {
            RestApiUtil.handleBadRequest("Required file " + fileName + " is not provided", log);
            return null;
        } else if (retrievedFiles.length > 1) {
            RestApiUtil.handleBadRequest("More than one " + fileName + " is provided", log);
            return null;
        }
        return retrievedFiles[0];
    }

    public static String generateServiceKey(ServiceEntry serviceEntry) {
        String key = serviceEntry.getName() + APIConstants.KEY_SEPARATOR + serviceEntry.getVersion();
        return key.toLowerCase();
    }

    /**
     * Convert list of ServiceInfoDTO objects to ServiceInfoListDTO object
     *
     * @param servicesList list of ServiceInfoDTO objects
     * @return build the ServiceInfoListDTO DTO object
     */
    public static ServiceInfoListDTO fromServiceInfoDTOToServiceInfoListDTO(List<ServiceInfoDTO> servicesList) {
        ServiceInfoListDTO serviceInfoListDTO = new ServiceInfoListDTO();

        serviceInfoListDTO.setCount(servicesList.size());
        serviceInfoListDTO.setList(servicesList);

        return serviceInfoListDTO;
    }

    /**
     * Convert entries in Hash Map to list of ServiceInfoDTO objects
     *
     * @param serviceList List of services provided in zip
     * @return build the List<ServiceInfoDTO> list
     */
    public static List<ServiceInfoDTO> fromServiceListToDTOList(List<ServiceEntry> serviceList) {
        List<ServiceInfoDTO> serviceInfoDTOList = new ArrayList<>();
        for (ServiceEntry service: serviceList) {
            serviceInfoDTOList.add(fromServiceEntryToServiceInfoDTO(service));
        }
        return serviceInfoDTOList;
    }

    /**
     * Converts ServiceEntry object to ServiceInfoDTO object
     *
     * @param serviceEntry ServiceEntry model object
     * @return Converted ServiceInfoDTO object
     */
    public static ServiceInfoDTO fromServiceEntryToServiceInfoDTO(ServiceEntry serviceEntry) {
        ServiceInfoDTO serviceInfoDTO = new ServiceInfoDTO();

        serviceInfoDTO.setId(serviceEntry.getUuid());
        serviceInfoDTO.setName(serviceEntry.getName());
        serviceInfoDTO.setKey(serviceEntry.getKey());
        serviceInfoDTO.setVersion(serviceEntry.getVersion());
        serviceInfoDTO.setMd5(serviceEntry.getMd5());

        return serviceInfoDTO;
    }

    public static ServiceDTO fromServiceToDTO(ServiceEntry service, boolean shrink) {
        ServiceDTO serviceDTO = new ServiceDTO();
        serviceDTO.setId(service.getUuid());
        serviceDTO.setName(service.getName());
        serviceDTO.setVersion(service.getVersion());
        serviceDTO.setMd5(service.getMd5());
        serviceDTO.setServiceKey(service.getKey());
        if (!shrink) {
            serviceDTO.setServiceUrl(service.getServiceUrl());
            serviceDTO.setDefinitionType(ServiceDTO.DefinitionTypeEnum.fromValue(service.getDefinitionType()
                    .toString()));
            serviceDTO.setDefinitionUrl(service.getDefUrl());
            serviceDTO.setDescription(service.getDescription());
            serviceDTO.setSecurityType(ServiceDTO.SecurityTypeEnum.fromValue(service.getSecurityType().toString()));
            serviceDTO.setMutualSSLEnabled(service.isMutualSSLEnabled());
            serviceDTO.setCreatedTime(String.valueOf(service.getCreatedTime()));
            serviceDTO.setLastUpdatedTime(String.valueOf(service.getLastUpdatedTime()));
            serviceDTO.setUsage(service.getUsage());
        }
        return serviceDTO;
    }
    /**
     * Convert list of ServiceInfoDTO objects to ServiceInfoListDTO object
     *
     * @param servicesList metadata list of services provided in zip
     * @return build the ServiceInfoListDTO object
     */
    public static ServiceInfoListDTO getServicesResponsePayloadBuilder(List<ServiceInfoDTO> servicesList) {
        ServiceInfoListDTO serviceInfoListDTO = new ServiceInfoListDTO();

        serviceInfoListDTO.setCount(servicesList.size());
        serviceInfoListDTO.setList(servicesList);

        return serviceInfoListDTO;
    }

    /**
     * Converts ServiceEntry object's input stream entries to files
     *
     * @param serviceEntry Service catalog entry
     * @return location to the files
     */
    public static String generateServiceFiles(ServiceEntry serviceEntry) {
        String pathToCreateFiles = FileBasedServicesImportExportManager.createDir(RestApiConstants.JAVA_IO_TMPDIR);
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String metadataString = gson.toJson(serviceEntry);
            JSONParser jsonParser = new JSONParser();
            org.json.simple.JSONObject metadataJson = (org.json.simple.JSONObject) jsonParser.parse(metadataString);
            metadataJson.remove("endpointDef");
            metadataString = CommonUtil.jsonToYaml(gson.toJson(metadataJson));
            fromInputStreamToFile(new ByteArrayInputStream(metadataString.getBytes()), pathToCreateFiles
                    + File.separator + APIConstants.METADATA_FILE);
            fromInputStreamToFile(serviceEntry.getEndpointDef(), pathToCreateFiles + File.separator +
                    APIConstants.DEFINITION_FILE);
        } catch (ParseException | IOException e) {
            RestApiUtil.handleInternalServerError("Error while generating the zip file", log);
        }
        return pathToCreateFiles;
    }

    /**
     * Create Service Filter Params object based on the parameters
     * @param name Service name
     * @param version Service version
     * @param definitionType Service Definition Type
     * @param key Service key
     * @param sortBy Sort By
     * @param sortOrder Sort Order
     * @param limit
     * @param offset
     * @return
     */
    public static ServiceFilterParams getServiceFilterParams(String name, String version, String definitionType,
                                                             String key, String sortBy, String sortOrder, Integer limit,
                                                             Integer offset) {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        sortOrder = sortOrder != null ? sortOrder : RestApiConstants.DEFAULT_SORT_ORDER;
        sortBy = getServiceSortByField(sortBy);
        name = name != null ? name : StringUtils.EMPTY;
        version = version != null ? version : StringUtils.EMPTY;
        definitionType = definitionType != null ? definitionType : StringUtils.EMPTY;
        key = key != null ? key : StringUtils.EMPTY;

        ServiceFilterParams filterParams = new ServiceFilterParams();
        filterParams.setName(name);
        filterParams.setVersion(version);
        filterParams.setDefinitionType(definitionType);
        filterParams.setKey(key);
        filterParams.setSortBy(sortBy);
        filterParams.setSortOrder(sortOrder);
        filterParams.setLimit(limit);
        filterParams.setOffset(offset);
        return filterParams;
    }

    public static void setPaginationParams(ServiceListDTO serviceListDTO, int offset, int limit, int size,
                                           ServiceFilterParams filterParams) {
        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, size);
        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = getServicesPaginatedUrl(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), filterParams);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = getServicesPaginatedUrl(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), filterParams);
        }
        PaginationDTO paginationDTO = getPaginationDTO(limit, offset, size, paginatedNext, paginatedPrevious);
        serviceListDTO.setPagination(paginationDTO);
    }

    public static APIInfoDTO fromAPIToAPIInfoDTO(API api) {
        APIInfoDTO apiInfoDTO = new APIInfoDTO();
        apiInfoDTO.setName(api.getId().getApiName());
        apiInfoDTO.setVersion(api.getId().getVersion());
        apiInfoDTO.setContext(api.getContext());
        apiInfoDTO.setId(api.getUuid());
        apiInfoDTO.setProvider(api.getId().getProviderName());
        return apiInfoDTO;
    }

    private static String getServiceSortByField(String sortBy) {
        String updatedSortBy = StringUtils.EMPTY;
        // Default sortBy field is name
        if (sortBy == null || "name".equals(sortBy)) {
            updatedSortBy = APIConstants.ServiceCatalogConstants.SERVICE_NAME;
        } else if ("definitionType".equals(sortBy)) {
            updatedSortBy = APIConstants.ServiceCatalogConstants.DEFINITION_TYPE;
        }
        return updatedSortBy;
    }

    private static PaginationDTO getPaginationDTO(int limit, int offset, int total, String next, String previous) {
        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setLimit(limit);
        paginationDTO.setOffset(offset);
        paginationDTO.setTotal(total);
        paginationDTO.setNext(next);
        paginationDTO.setPrevious(previous);
        return paginationDTO;
    }

    private static String getServicesPaginatedUrl(Integer offset, Integer limit, ServiceFilterParams filterParams) {
        String sortBy = "SERVICE_NAME".equals(filterParams.getSortBy()) ? "name" : "definitionType";
        return  "/services?name=" + filterParams.getName() + "&version=" + filterParams.getVersion()
                + "&definitionType=" + filterParams.getDefinitionType() + "&displayName="
                + filterParams.getDisplayName() + "&key=" + filterParams.getKey() + "&sortBy="
                + sortBy + "&sortOrder=" + filterParams.getSortOrder() + "&limit=" + limit
                + "&offset=" + offset;
    }

    /**
     * Write ServiceEntry object's input stream entries to files
     *
     * @param inputStream inputStream of files
     * @param outputFile  output file name
     * @return location to the files
     */
    private static void fromInputStreamToFile(InputStream inputStream, String outputFile) {
        File file = new File(outputFile);
        try (OutputStream outputStream = new FileOutputStream(file)) {
            IOUtils.copy(inputStream, outputStream);
        } catch (IOException e) {
            RestApiUtil.handleInternalServerError("Error while preparing resource files before zip", e, log);
        }
    }
}
