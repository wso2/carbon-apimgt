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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.FileUtils;
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
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
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
    public static ServiceEntry fromFileToServiceInfo(File file) throws IOException {

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
     * Generate Hash Map to hold all the ServiceEntry objects relevant to the services included in zip
     *
     * @param path path to the directory which include files
     * @return HashMap with service key as key and ServiceEntry object as value
     */
    public static HashMap<String, ServiceEntry> fromDirToServiceInfoMap(String path) {
        HashMap<String, ServiceEntry> endpointDetails = new HashMap<>();
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
                            serviceInfo = fromFileToServiceInfo(aFile);
                            if (!StringUtils.isBlank(serviceInfo.getKey())) {
                                key = serviceInfo.getKey();
                            } else {
                                key = generateServiceKey(serviceInfo);
                            }
                            serviceInfo.setKey(key);
                            serviceInfo.setMetadata(new ByteArrayInputStream(FileUtils.readFileToByteArray(aFile)));
                        } else {
                            serviceInfo.setEndpointDef(new ByteArrayInputStream(FileUtils.readFileToByteArray(aFile)));
                        }
                    }
                } catch (IOException e) {
                    RestApiUtil.handleInternalServerError("Error while reading service resource files. " +
                            "Zip might not include valid data", e, log);
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
     * @param catalogEntries Hash Map of services provided in zip
     * @return build the List<ServiceInfoDTO> list
     */
    public static List<ServiceInfoDTO> fromServiceEntryToDTOList(HashMap<String, ServiceEntry> catalogEntries) {
        List<ServiceInfoDTO> serviceStatusList = new ArrayList<>();
        for (Map.Entry<String, ServiceEntry> entry : catalogEntries.entrySet()) {
            serviceStatusList.add(ServiceEntryMappingUtil.fromServiceEntryToServiceInfoDTO(
                    catalogEntries.get(entry.getKey())));
        }
        return serviceStatusList;
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
        fromInputStreamToFile(serviceEntry.getMetadata(), pathToCreateFiles + File.separator +
                APIConstants.METADATA_FILE);
        fromInputStreamToFile(serviceEntry.getEndpointDef(), pathToCreateFiles + File.separator +
                APIConstants.DEFINITION_FILE);

        return pathToCreateFiles;
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
