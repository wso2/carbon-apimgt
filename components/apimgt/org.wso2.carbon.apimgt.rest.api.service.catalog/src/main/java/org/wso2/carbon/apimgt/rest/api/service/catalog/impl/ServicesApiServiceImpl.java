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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.api.model.ServiceEntry;
import org.wso2.carbon.apimgt.api.model.ServiceFilterParams;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.ServiceCatalogImpl;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.service.catalog.ServicesApiService;
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
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ServicesApiServiceImpl implements ServicesApiService {

    private static final Log log = LogFactory.getLog(ServicesApiServiceImpl.class);
    private static final ServiceCatalogImpl serviceCatalog = new ServiceCatalogImpl();

    public Response createService(ServiceDTO catalogEntry, InputStream definitionFileInputStream,
                                  Attachment definitionFileDetail, MessageContext messageContext) {
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    public Response deleteService(String serviceId, MessageContext messageContext) {
        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        try {
            serviceCatalog.deleteService(serviceId, tenantId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting the service with key " + serviceId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

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
            serviceEntry = serviceCatalog.getEndPointResourcesByNameAndVersion(name, version, tenantId);
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

    public Response getServiceDefinition(String serviceId, MessageContext messageContext) {
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    @Override
    public Response importService(InputStream fileInputStream, Attachment fileDetail, Boolean overwrite,
                                  String verifier, MessageContext messageContext) throws APIManagementException {
        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        String tempDirPath = FileBasedServicesImportExportManager.createDir(RestApiConstants.JAVA_IO_TMPDIR);
        List<ServiceInfoDTO> serviceStatusList;
        HashMap<String, ServiceEntry> catalogEntries;
        HashMap<String, String> newResourcesHash;

        // unzip the uploaded zip
        try {
            FileBasedServicesImportExportManager importExportManager =
                    new FileBasedServicesImportExportManager(tempDirPath);
            importExportManager.importService(fileInputStream);
        } catch (APIMgtResourceAlreadyExistsException e) {
            RestApiUtil.handleResourceAlreadyExistsError("Error while importing Service", e, log);
        }

        newResourcesHash = Md5HashGenerator.generateHash(tempDirPath);
        catalogEntries = ServiceEntryMappingUtil.fromDirToServiceInfoMap(tempDirPath);

        HashMap<String, ServiceEntry> serviceEntries = new HashMap<>();
        for (Map.Entry<String, ServiceEntry> entry : catalogEntries.entrySet()) {
            String key = entry.getKey();
            catalogEntries.get(key).setMd5(newResourcesHash.get(key));
            try {
                if (ServiceCatalogUtils.checkServiceExistence(key, tenantId)) {
                    if (overwrite) {
                        serviceCatalog.updateService(catalogEntries.get(key), tenantId, userName);
                    } else {
                        return Response.status(Response.Status.CONFLICT).build();
                    }
                } else {
                    serviceCatalog.addService(catalogEntries.get(key), tenantId, userName);
                }
                ServiceEntry serviceEntry = serviceCatalog.getServiceByKey(key, tenantId);
                serviceEntries.put(key, serviceEntry);
            } catch (APIManagementException e) {
                // client will only be informed by the list of successfully added services
                log.error("Failed to add or update service key: " + key + " since " + e.getMessage(), e);
            }
        }
        serviceStatusList = ServiceEntryMappingUtil.fromServiceEntryToDTOList(serviceEntries);
        return Response.ok().entity(ServiceEntryMappingUtil
                .fromServiceInfoDTOToServiceInfoListDTO(serviceStatusList)).build();
    }

    @Override
    public Response searchServices(String name, String version, String definitionType, String displayName,
                                   String key, Boolean shrink, String sortBy, String sortOrder, Integer limit,
                                   Integer offset, MessageContext messageContext) throws APIManagementException {
        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        try {
            List<ServiceDTO> serviceDTOList = new ArrayList<>();
            ServiceFilterParams filterParams = ServiceEntryMappingUtil.getServiceFilterParams(name, version, definitionType,
                    displayName, key, sortBy, sortOrder, limit, offset);
            List<ServiceEntry> services = serviceCatalog.getServices(filterParams, tenantId, shrink);
            for (ServiceEntry service : services) {
                serviceDTOList.add(ServiceEntryMappingUtil.fromServiceToDTO(service, shrink));
            }
            ServiceListDTO serviceListDTO = new ServiceListDTO();
            serviceListDTO.setList(serviceDTOList);
            ServiceEntryMappingUtil.setPaginationParams(serviceListDTO, filterParams.getOffset(), filterParams.getLimit(),
                    serviceDTOList.size(), filterParams);
            return Response.ok().entity(serviceListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Services";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    public Response updateService(String serviceId, ServiceDTO catalogEntry, InputStream definitionFileInputStream,
                                  Attachment definitionFileDetail, MessageContext messageContext) {
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented for updating services");
        return Response.status(status).entity(errorObject).build();
    }
}
