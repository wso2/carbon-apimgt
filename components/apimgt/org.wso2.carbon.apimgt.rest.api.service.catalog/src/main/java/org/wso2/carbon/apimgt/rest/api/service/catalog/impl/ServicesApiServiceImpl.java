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

package org.wso2.carbon.apimgt.rest.api.service.catalog.impl;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.ServiceEntry;
import org.wso2.carbon.apimgt.api.model.ServiceFilterParams;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.ServiceCatalogImpl;
import org.wso2.carbon.apimgt.impl.definitions.AsyncApiParserUtil;
import org.wso2.carbon.apimgt.impl.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.utils.APIMWSDLReader;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.service.catalog.ServicesApiService;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceInfoDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceListDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.model.ExportArchive;
import org.wso2.carbon.apimgt.rest.api.service.catalog.utils.FileBasedServicesImportExportManager;
import org.wso2.carbon.apimgt.rest.api.service.catalog.utils.Md5HashGenerator;
import org.wso2.carbon.apimgt.rest.api.service.catalog.utils.ServiceCatalogUtils;
import org.wso2.carbon.apimgt.rest.api.service.catalog.utils.ServiceEntryMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ServicesApiServiceImpl implements ServicesApiService {

    private static final Log log = LogFactory.getLog(ServicesApiServiceImpl.class);
    private static final ServiceCatalogImpl serviceCatalog = new ServiceCatalogImpl();

    @Override
    public Response addService(ServiceDTO serviceDTO, InputStream definitionFileInputStream,
                               Attachment definitionFileDetail, String inlineContent, MessageContext messageContext) {
        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        try {
            validateInputParams(definitionFileInputStream, definitionFileDetail, inlineContent);
            ServiceEntry existingService = serviceCatalog.getServiceByKey(serviceDTO.getServiceKey(), tenantId);
            if (existingService != null) {
                RestApiUtil.handleResourceAlreadyExistsError("Error while adding Service : A service already "
                        + "exists with key: " + serviceDTO.getServiceKey(), log);
            }
            byte[] definitionFileByteArray;
            if (definitionFileInputStream != null) {
                definitionFileByteArray = getDefinitionFromInput(definitionFileInputStream);
            } else {
                definitionFileByteArray = inlineContent.getBytes();
            }
            ServiceEntry service = ServiceCatalogUtils.createServiceFromDTO(serviceDTO, definitionFileByteArray);
            if (!validateAndRetrieveServiceDefinition(definitionFileByteArray, serviceDTO.getServiceUrl(),
                    service.getDefinitionType()).isValid()) {
                String errorMsg = "The Service import has been failed as invalid service definition provided";
                return Response.status(Response.Status.BAD_REQUEST).entity(getErrorDTO(RestApiConstants
                        .STATUS_BAD_REQUEST_MESSAGE_DEFAULT, 400L, errorMsg, StringUtils.EMPTY)).build();
            }
            String serviceId = serviceCatalog.addService(service, tenantId, userName);
            ServiceEntry createdService = serviceCatalog.getServiceByUUID(serviceId, tenantId);
            return Response.ok().entity(ServiceEntryMappingUtil.fromServiceToDTO(createdService, false)).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error when validating the service definition", log);
        } catch (IOException e) {
            RestApiUtil.handleInternalServerError("Error when reading the file content", log);
        }
        return null;
    }

    @Override
    public Response deleteService(String serviceId, MessageContext messageContext) {
        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        try {
            List<API> usedAPIs = serviceCatalog.getServiceUsage(serviceId, tenantId);
            if (usedAPIs != null && usedAPIs.size() > 0 ) {
                String message = "Cannot remove the Service as it is used by one or more APIs";
                RestApiUtil.handleConflict(message, log);
            }
            serviceCatalog.deleteService(serviceId, tenantId);
            return Response.noContent().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting the service with key " + serviceId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response exportService(String name, String version, MessageContext messageContext) {
        File exportedServiceArchiveFile = null;
        // creates a directory in default temporary-file directory
        String pathToExportDir = FileBasedServicesImportExportManager.createDir(RestApiConstants.JAVA_IO_TMPDIR);
        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        String archiveName = name + APIConstants.KEY_SEPARATOR + version;
        ServiceEntry serviceEntry;
        String exportedFileName = null;
        ExportArchive exportArchive;

        if (StringUtils.isBlank(name) || StringUtils.isBlank(version)) {
            RestApiUtil.handleBadRequest("Service name or owner should not be empty or null.", log);
        }

        try {
            serviceEntry = serviceCatalog.getServiceByNameAndVersion(name, version, tenantId);
            if (serviceEntry != null) {
                FileBasedServicesImportExportManager importExportManager =
                        new FileBasedServicesImportExportManager(pathToExportDir);
                exportArchive = importExportManager.createArchiveFromExportedServices(
                        ServiceEntryMappingUtil.generateServiceFiles(serviceEntry),
                        pathToExportDir, archiveName);
                exportedServiceArchiveFile = new File(exportArchive.getArchiveName());
                exportedFileName = exportedServiceArchiveFile.getName();
                Response.ResponseBuilder responseBuilder =
                        Response.status(Response.Status.OK).entity(exportedServiceArchiveFile)
                                .type(MediaType.APPLICATION_OCTET_STREAM);
                responseBuilder.header("Content-Disposition", "attachment; filename=\"" + exportedFileName + "\"");
                return responseBuilder.build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while exporting Services: " + archiveName, e, log);
        }
        return null;
    }

    @Override
    public Response getServiceById(String serviceId, MessageContext messageContext) {
        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        try {
            ServiceEntry service = serviceCatalog.getServiceByUUID(serviceId, tenantId);
            ServiceDTO serviceDTO = ServiceEntryMappingUtil.fromServiceToDTO(service, false);
            return Response.ok().entity(serviceDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError("Service", serviceId, e, log);
            } else{
                RestApiUtil.handleInternalServerError("Error while fetching the Service with ID " + serviceId, e,
                        log);
            }
        }
        return null;
    }

    @Override
    public Response getServiceDefinition(String serviceId, MessageContext messageContext) {
        String user = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(user);
        String contentType = StringUtils.EMPTY;
        try {
            ServiceEntry service = serviceCatalog.getServiceByUUID(serviceId, tenantId);
            if (ServiceDTO.DefinitionTypeEnum.OAS3.equals(ServiceDTO.DefinitionTypeEnum.fromValue(service
                    .getDefinitionType().name())) || ServiceDTO.DefinitionTypeEnum.OAS2.equals(ServiceDTO
                    .DefinitionTypeEnum.fromValue(service.getDefinitionType().name())) || ServiceDTO.DefinitionTypeEnum
                    .ASYNC_API.equals(ServiceDTO.DefinitionTypeEnum.fromValue(service.getDefinitionType().name()))) {
                contentType = "application/yaml";
            } else if (ServiceDTO.DefinitionTypeEnum.WSDL1.equals(ServiceDTO.DefinitionTypeEnum.fromValue(service
                    .getDefinitionType().name()))) {
                contentType = "text/xml";
            }
            InputStream serviceDefinition = service.getEndpointDef();
            if (serviceDefinition == null) {
                RestApiUtil.handleResourceNotFoundError("Service definition not found for service with ID: "
                        + serviceId, log);
            } else {
                return Response.ok(serviceDefinition).type(contentType).build();
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError("Service", serviceId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while retrieving the definition" +
                        " of service with ID: " + serviceId, e, log);
            } else{
                RestApiUtil.handleInternalServerError("Error when retrieving the endpoint definition of service " +
                        "with id " + serviceId, e, log);
            }
        }
        return null;
    }

    @Override
    public Response importService(InputStream fileInputStream, Attachment fileDetail, Boolean overwrite,
                                  String verifier, MessageContext messageContext) throws APIManagementException {
        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        String tempDirPath = FileBasedServicesImportExportManager.createDir(RestApiConstants.JAVA_IO_TMPDIR);
        List<ServiceInfoDTO> serviceList;
        HashMap<String, ServiceEntry> serviceEntries;
        HashMap<String, String> newResourcesHash;
        List<ServiceEntry> serviceListToImport = new ArrayList<>();
        List<ServiceEntry> serviceListToIgnore = new ArrayList<>();
        List<ServiceEntry> servicesWithInvalidDefinition = new ArrayList<>();

        // unzip the uploaded zip
        try {
            FileBasedServicesImportExportManager importExportManager =
                    new FileBasedServicesImportExportManager(tempDirPath);
            importExportManager.importService(fileInputStream);
        } catch (APIMgtResourceAlreadyExistsException e) {
            RestApiUtil.handleResourceAlreadyExistsError("Error while importing Service", e, log);
        }

        newResourcesHash = Md5HashGenerator.generateHash(tempDirPath);
        serviceEntries = ServiceEntryMappingUtil.fromDirToServiceEntryMap(tempDirPath);
        Map<String, Boolean> validationResults = new HashMap<>();
        if (overwrite && StringUtils.isNotEmpty(verifier)) {
            validationResults = validateVerifier(verifier, tenantId);
        }
        try {
            for (Map.Entry<String, ServiceEntry> entry : serviceEntries.entrySet()) {
                String key = entry.getKey();
                serviceEntries.get(key).setMd5(newResourcesHash.get(key));
                ServiceEntry service = serviceEntries.get(key);
                byte[] definitionFileByteArray = getDefinitionFromInput(service.getEndpointDef());
                if (validateAndRetrieveServiceDefinition(definitionFileByteArray, service.getDefUrl(),
                        service.getDefinitionType()).isValid() ||
                        (ServiceEntry.DefinitionType.WSDL1.equals(service.getDefinitionType())
                                && APIMWSDLReader.validateWSDLFile(definitionFileByteArray).isValid())) {
                    service.setEndpointDef(new ByteArrayInputStream(definitionFileByteArray));
                } else {
                    servicesWithInvalidDefinition.add(service);
                }
                if (overwrite) {
                    if (StringUtils.isNotEmpty(verifier) && validationResults
                            .containsKey(service.getKey()) && !validationResults.get(service.getKey())) {
                        serviceListToIgnore.add(service);
                    } else {
                        serviceListToImport.add(service);
                    }
                } else {
                    serviceListToImport.add(service);
                }
            }
        } catch (IOException e) {
            RestApiUtil.handleInternalServerError("Error when reading the service definition content", log);
        }
        if (servicesWithInvalidDefinition.size() > 0) {
            serviceList = ServiceEntryMappingUtil.fromServiceListToDTOList(servicesWithInvalidDefinition);
            String errorMsg = "The Service import has been failed as invalid service definition provided";
            return Response.status(Response.Status.BAD_REQUEST).entity(getErrorDTO(RestApiConstants
            .STATUS_BAD_REQUEST_MESSAGE_DEFAULT, 400L, errorMsg, new JSONArray(serviceList).toString())).build();
        }
        if (serviceListToIgnore.size() > 0) {
            serviceList = ServiceEntryMappingUtil.fromServiceListToDTOList(serviceListToIgnore);
            String errorMsg = "The Service import has been failed since to verifier validation fails";

            return Response.status(Response.Status.BAD_REQUEST).entity(getErrorDTO(RestApiConstants
            .STATUS_BAD_REQUEST_MESSAGE_DEFAULT, 400L, errorMsg, new JSONArray(serviceList).toString())).build();
        } else {
            List<ServiceEntry> importedServiceList = new ArrayList<>();
            List<ServiceEntry> retrievedServiceList = new ArrayList<>();
            try {
                if (serviceListToImport.size() > 0) {
                    importedServiceList = serviceCatalog.importServices(serviceListToImport, tenantId, userName,
                            overwrite);
                }
            } catch (APIManagementException e) {
                if (ExceptionCodes.SERVICE_IMPORT_FAILED_WITHOUT_OVERWRITE.getErrorCode() == e.getErrorHandler()
                        .getErrorCode()) {
                    RestApiUtil.handleBadRequest("Cannot update existing services when overwrite is false", log);
                } else {
                    RestApiUtil.handleInternalServerError("Error when importing services to service catalog",
                            e, log);
                }
            }
            if (importedServiceList == null) {
                RestApiUtil.handleBadRequest("Cannot update the name or version or key or definition type of an " +
                        "existing service", log);
            }
            for (ServiceEntry service : importedServiceList) {
                retrievedServiceList.add(serviceCatalog.getServiceByKey(service.getKey(), tenantId));
            }
            serviceList = ServiceEntryMappingUtil.fromServiceListToDTOList(retrievedServiceList);
            return Response.ok().entity(ServiceEntryMappingUtil
                    .fromServiceInfoDTOToServiceInfoListDTO(serviceList)).build();
        }
    }

    @Override
    public Response searchServices(String name, String version, String definitionType, String key, Boolean shrink,
                                   String sortBy, String sortOrder, Integer limit, Integer offset,
                                   MessageContext messageContext) throws APIManagementException {
        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        try {
            List<ServiceDTO> serviceDTOList = new ArrayList<>();
            ServiceFilterParams filterParams = ServiceEntryMappingUtil.getServiceFilterParams(name, version,
                    definitionType, key, sortBy, sortOrder, limit, offset);
            List<ServiceEntry> services = serviceCatalog.getServices(filterParams, tenantId, shrink);
            int totalServices = serviceCatalog.getServicesCount(tenantId, filterParams);
            for (ServiceEntry service : services) {
                serviceDTOList.add(ServiceEntryMappingUtil.fromServiceToDTO(service, shrink));
            }
            ServiceListDTO serviceListDTO = new ServiceListDTO();
            serviceListDTO.setList(serviceDTOList);
            ServiceEntryMappingUtil.setPaginationParams(serviceListDTO, filterParams.getOffset(), filterParams.getLimit(),
                    totalServices, filterParams);
            return Response.ok().entity(serviceListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Services";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response getServiceUsage(String serviceId, MessageContext messageContext) {
        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        try {
            List<API> apiList = serviceCatalog.getServiceUsage(serviceId, tenantId);
            if (apiList != null) {
                APIListDTO apiListDTO = new APIListDTO();
                List<APIInfoDTO> apiInfoDTOList = new ArrayList<>();
                for (API api : apiList) {
                    apiInfoDTOList.add(ServiceEntryMappingUtil.fromAPIToAPIInfoDTO(api));
                }
                apiListDTO.setList(apiInfoDTOList);
                apiListDTO.setCount(apiList.size());
                return Response.ok().entity(apiListDTO).build();
            } else {
                RestApiUtil.handleResourceNotFoundError("Service", serviceId, log);
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API usage of service";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response updateService(String serviceId, ServiceDTO serviceDTO, InputStream definitionFileInputStream,
                              Attachment definitionFileDetail, String inlineContent, MessageContext messageContext) {
        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        if (StringUtils.isEmpty(serviceId)) {
            RestApiUtil.handleBadRequest("The service Id should not be empty", log);
        }
        validateInputParams(definitionFileInputStream, definitionFileDetail, inlineContent);
        try {
            ServiceEntry existingService = serviceCatalog.getServiceByUUID(serviceId, tenantId);
            byte[] definitionFileByteArray;
            if (definitionFileInputStream != null) {
                definitionFileByteArray = getDefinitionFromInput(definitionFileInputStream);
            } else {
                definitionFileByteArray = inlineContent.getBytes();
            }
            ServiceEntry service = ServiceCatalogUtils.createServiceFromDTO(serviceDTO, definitionFileByteArray);
            if (!validateAndRetrieveServiceDefinition(definitionFileByteArray, serviceDTO.getServiceUrl(),
                    service.getDefinitionType()).isValid()) {
                String errorMsg = "The Service import has been failed as invalid service definition provided";
                return Response.status(Response.Status.BAD_REQUEST).entity(getErrorDTO(RestApiConstants
                        .STATUS_BAD_REQUEST_MESSAGE_DEFAULT, 400L, errorMsg, StringUtils.EMPTY)).build();
            }
            if (!existingService.getKey().equals(service.getKey()) || !existingService.getName().equals(service
                .getName()) || !existingService.getDefinitionType().equals(service.getDefinitionType()) ||
                    !existingService.getVersion().equals(service.getVersion())) {
                RestApiUtil.handleBadRequest("Cannot update the name or version or key or definition type of an " +
                        "existing service", log);
            }
            service.setUuid(existingService.getUuid());
            serviceCatalog.updateService(service, tenantId, userName);
            ServiceEntry createdService = serviceCatalog.getServiceByUUID(serviceId, tenantId);
            return Response.ok().entity(ServiceEntryMappingUtil.fromServiceToDTO(createdService, false)).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError("Service", serviceId, e, log);
            }
            RestApiUtil.handleInternalServerError("Error when validating the service definition", log);
        } catch (IOException e) {
            RestApiUtil.handleInternalServerError("Error when reading the file content", log);
        }
        return null;
    }

    private Map<String, Boolean> validateVerifier(String verifier, int tenantId) throws APIManagementException {
        Map<String, Boolean> validationResults = new HashMap<>();
        JSONArray verifierArray = new JSONArray(verifier);
        for (int i = 0; i < verifierArray.length() ; i++) {
            JSONObject verifierJson = verifierArray.getJSONObject(i);
            ServiceEntry service = serviceCatalog.getServiceByKey(verifierJson.get("key").toString(), tenantId);
            if (service != null) {
                if (service.getMd5().equals(verifierJson.get("md5").toString())) {
                    validationResults.put(service.getKey(), true);
                } else {
                    validationResults.put(service.getKey(), false);
                }
            }
        }
        return validationResults;
    }

    private boolean isAuthorizationFailure(Exception e) {
        String errorMessage = e.getMessage();
        return errorMessage != null && errorMessage.contains(APIConstants.UN_AUTHORIZED_ERROR_MESSAGE);
    }

    /**
     * Validate the ASYNC API definition provided
     * @param url Service Definition URL
     * @param definitionContent Service Definition Content
     * @return APIDefinitionValidationResponse
     * @throws APIManagementException
     */
    private APIDefinitionValidationResponse validateAsyncAPISpecification(String url, String definitionContent)
            throws APIManagementException, IOException {
        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();
        if (StringUtils.isNotEmpty(definitionContent)){
            //validate file
            //convert .yml or .yaml to JSON for validation
            String schemaToBeValidated = CommonUtil.yamlToJson(definitionContent);
            validationResponse = AsyncApiParserUtil.validateAsyncAPISpecification(schemaToBeValidated, true);
        } else if (url != null) {
            validationResponse = AsyncApiParserUtil.validateAsyncAPISpecificationByURL(url, true);
        }
        return validationResponse;
    }

    /**
     * Validate the Open API definition provided
     * @param url Service Definition URL
     * @param definitionContent Service Definition Content
     * @return APIDefinitionValidationResponse
     * @throws APIManagementException
     */
    private APIDefinitionValidationResponse validateOpenAPIDefinition(String url, String definitionContent)
            throws APIManagementException {
        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();
        if (definitionContent != null) {
            validationResponse = OASParserUtil.validateAPIDefinition(definitionContent, true);
        } else if (url != null) {
            validationResponse = OASParserUtil.validateAPIDefinitionByURL(url, true);
        }
        return validationResponse;
    }

    private static ErrorDTO getErrorDTO(String message, Long code, String description, String info){
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setCode(code);
        errorDTO.setMessage(message);
        errorDTO.setMoreInfo(info);
        errorDTO.setDescription(description);
        return errorDTO;
    }

    private APIDefinitionValidationResponse validateAndRetrieveServiceDefinition(byte[] definitionFileByteArray,
                             String url, ServiceEntry.DefinitionType type) throws APIManagementException, IOException {
        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();
        String definitionContent = new String(definitionFileByteArray);
        if (ServiceEntry.DefinitionType.OAS3.equals(type) || ServiceEntry.DefinitionType.OAS2.equals(type)) {
            validationResponse = validateOpenAPIDefinition(url, definitionContent);
        } else if (ServiceEntry.DefinitionType.ASYNC_API.equals(type)) {
            validationResponse = validateAsyncAPISpecification(url, definitionContent);
        }
        return validationResponse;
    }

    private byte[] getDefinitionFromInput(InputStream definitionFileInputStream) throws IOException {

        ByteArrayOutputStream definitionFileOutputByteStream = new ByteArrayOutputStream();
        IOUtils.copy(definitionFileInputStream, definitionFileOutputByteStream);
        return definitionFileOutputByteStream.toByteArray();
    }

    private void validateInputParams(InputStream definitionInputStream, Attachment fileDetail, String inlineContent) {
        boolean isFileSpecified = definitionInputStream != null && fileDetail != null &&
                fileDetail.getContentDisposition() != null && fileDetail.getContentDisposition().getFilename() != null;
        if (inlineContent == null && !isFileSpecified) {
            RestApiUtil.handleBadRequest("Either inline definition or file should be provided", log);
        }
        if (inlineContent != null && isFileSpecified) {
            RestApiUtil.handleBadRequest("Only one of inline definition or file should be provided", log);
        }
    }
}
