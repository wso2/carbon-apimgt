package org.wso2.carbon.apimgt.rest.api.service.catalog.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.api.model.ServiceCatalogInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.ServiceCatalogImpl;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.service.catalog.ServiceEntriesApiService;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.*;
import org.wso2.carbon.apimgt.rest.api.service.catalog.model.ExportArchive;
import org.wso2.carbon.apimgt.rest.api.service.catalog.utils.DataMappingUtil;
import org.wso2.carbon.apimgt.rest.api.service.catalog.utils.FileBasedServicesImportExportManager;
import org.wso2.carbon.apimgt.rest.api.service.catalog.utils.Md5HashGenerator;
import org.wso2.carbon.apimgt.rest.api.service.catalog.utils.ServiceCatalogUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


public class ServiceEntriesApiServiceImpl implements ServiceEntriesApiService {

    private static final Log log = LogFactory.getLog(ServiceEntriesApiServiceImpl.class);
    private static final ServiceCatalogImpl serviceCatalog = new ServiceCatalogImpl();
    public final String RESOURCE_FOLDER_LOCATION = "repository" + File.separator + "data" + File.separator + "petstore";
    public final String ZIP_EXPORT_DIR = "repository" + File.separator + "data";
    private final String ENDPOINT_NAME = "petstore-endpoint";
    private final String ENDPOINT_VERSION = "1.0.0";
    private final String OAS_FILE_NAME = "oas.yaml";
    private final String METADATA_FILE_NAME = "metadata.yaml";
    private final String DASH = "-";

    public Response checkServiceExistence(String name, String version, MessageContext messageContext) {
        if (Files.exists(Paths.get(RESOURCE_FOLDER_LOCATION))) {
            if (StringUtils.equals(name, ENDPOINT_NAME) && StringUtils.equals(version, ENDPOINT_VERSION)) {
                List<File> fileList = new ArrayList<File>();
                fileList.add(new File(RESOURCE_FOLDER_LOCATION + File.separator + METADATA_FILE_NAME));
                fileList.add(new File(RESOURCE_FOLDER_LOCATION + File.separator + OAS_FILE_NAME));
//                String eTag = Md5HashGenerator.generateHash(fileList);

//                return Response.ok().header("ETag", eTag).build();
            } else {
                RestApiUtil.handleBadRequest("Invalid service name or version defined", log);
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response checkServicesExistence(String key, Boolean shrink, MessageContext messageContext) throws APIManagementException {
        if (StringUtils.isBlank(key)) {
            RestApiUtil.handleBadRequest("Service key can not be an empty String", log);
        }

        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        List<ServiceInfoDTO> servicesList = new ArrayList<>();
        PaginationDTO paginationDTO = null;

        String keys[] = key.trim().split("\\s*,\\s*");
        for (String serviceKey : keys) {
            ServiceCatalogInfo serviceCatalogInfo = serviceCatalog.getServiceByKey(serviceKey, tenantId);
            if (serviceCatalogInfo != null) {
                servicesList.add(DataMappingUtil.fromServiceCatalogInfoToServiceInfoDTO(serviceCatalogInfo));
            }
        }
        return Response.ok().entity(DataMappingUtil.getServicesResponsePayloadBuilder(servicesList)).build();// set paginationDTO
    }

    public Response createService(ServiceDTO catalogEntry, InputStream definitionFileInputStream, Attachment definitionFileDetail, MessageContext messageContext) {
        log.info("createService");

        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    public Response deleteService(String serviceId, MessageContext messageContext) {
        log.info("deleteService");

        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    public Response exportService(String name, String version, MessageContext messageContext) {
        APIConsumer consumer;
        File exportedServiceArchiveFile = null;
        String pathToExportDir = FileBasedServicesImportExportManager.directoryCreator(RestApiConstants.JAVA_IO_TMPDIR); //creates a directory in default temporary-file directory
        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        String archiveName = name + APIConstants.KEY_SEPARATOR + version;
        ServiceCatalogInfo serviceCatalogInfo;
        String exportedFileName = null;
        ExportArchive exportArchive;

        if (StringUtils.isBlank(name) || StringUtils.isBlank(version)) {
            RestApiUtil.handleBadRequest("Service name or owner should not be empty or null.", log);
        }

        try {
            serviceCatalogInfo = serviceCatalog.getEndPointResourcesByNameAndVersion(name, version, tenantId);
            if (serviceCatalogInfo != null) {
                consumer = RestApiCommonUtil.getConsumer(userName);
                FileBasedServicesImportExportManager importExportManager =
                        new FileBasedServicesImportExportManager(consumer, pathToExportDir);
                exportArchive = importExportManager.createArchiveFromExportedServices(DataMappingUtil.filesGenerator(serviceCatalogInfo),
                        pathToExportDir, archiveName);
                exportedServiceArchiveFile = new File(exportArchive.getArchiveName());
                exportedFileName = exportedServiceArchiveFile.getName();
            } else {
                return Response.ok("Empty result set").build();
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while exporting Services: " + archiveName, e, log);
        }

        Response.ResponseBuilder responseBuilder =
                Response.status(Response.Status.OK).entity(exportedServiceArchiveFile).type(MediaType.APPLICATION_OCTET_STREAM);
        responseBuilder.header("Content-Disposition", "attachment; filename=\"" + exportedFileName + "\"");
        return responseBuilder.build();
    }

    public Response getServiceById(String serviceId, MessageContext messageContext) {
        log.info("getServiceById");

        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    public Response getServiceDefinition(String serviceId, MessageContext messageContext) {
        log.info("getServiceDefinition");

        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    @Override
    public Response importService(String serviceId, InputStream fileInputStream, Attachment fileDetail, String verifier, String ifMatch, Boolean overwrite, MessageContext messageContext) throws APIManagementException {
        APIConsumer consumer;
        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        String tempDirPath = FileBasedServicesImportExportManager.directoryCreator(RestApiConstants.JAVA_IO_TMPDIR);
        List<VerifierDTO> verifierJSONList = null;
        HashMap<String, String> newResourcesHash;
        HashMap<String, ServiceCatalogInfo> catalogEntries;
        HashMap<String, List<String>> existingServices;
        HashMap<String, List<String>> newServices;
        List<ServiceInfoDTO> serviceStatusList;

        // String to JSON conversion
        final ObjectMapper objectMapper = new ObjectMapper();
        try {
            VerifierDTO[] verifierJSONArray = objectMapper.readValue(verifier, VerifierDTO[].class);
            verifierJSONList = new ArrayList(Arrays.asList(verifierJSONArray));
        } catch (JsonProcessingException e) {
            RestApiUtil.handleInternalServerError("Error while converting verifier JSON String to JSON object", e, log);
        }

        // unzip the uploaded zip
        try {
            consumer = RestApiCommonUtil.getConsumer(userName);
            FileBasedServicesImportExportManager importExportManager =
                    new FileBasedServicesImportExportManager(consumer, tempDirPath);
            importExportManager.importService(fileInputStream);
        } catch (APIMgtResourceAlreadyExistsException e) {
            RestApiUtil.handleResourceAlreadyExistsError("Error while importing Service", e, log);
        }

        if (overwrite == null || !overwrite) {
            if (verifierJSONList != null && verifierJSONList.size() != DataMappingUtil.dirCount(tempDirPath)) {
                RestApiUtil.handleBadRequest("Number of elements in verifier must equals to number of directories in the zip archive.", log);
            }
            newResourcesHash = Md5HashGenerator.generateHash(tempDirPath);
            catalogEntries = DataMappingUtil.fromDirToServiceCatalogInfoMap(tempDirPath);

            existingServices = ServiceCatalogUtils.verifierListValidate(verifierJSONList, newResourcesHash, tenantId);
            if (!existingServices.get(APIConstants.MAP_KEY_IGNORED).isEmpty()){
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }
            newServices = ServiceCatalogUtils.filterNewServices(verifierJSONList, tenantId);

            // Adding new services
            List<String> keyList = newServices.get(APIConstants.MAP_KEY_ACCEPTED);
            for (String newService : keyList) {
                if (catalogEntries.containsKey(newService)) {
                    catalogEntries.get(newService).setMd5(newResourcesHash.get(newService));
                    String uuid = serviceCatalog.addService(catalogEntries.get(newService), tenantId);
                    if (uuid != null) {
                        catalogEntries.get(newService).setUuid(uuid);
                    } else {
                        newServices.get(APIConstants.MAP_KEY_IGNORED).add(newService);
                        newServices.get(APIConstants.MAP_KEY_ACCEPTED).remove(newService);
                    }
                }
            }

            // Adding updated services
            for (String updatedService : existingServices.get(APIConstants.MAP_KEY_VERIFIED)) {
                if (catalogEntries.containsKey(updatedService)) {
                    catalogEntries.get(updatedService).setMd5(newResourcesHash.get(updatedService));
                    String uuid = serviceCatalog.addService(catalogEntries.get(updatedService), tenantId);
                    if (uuid != null) {
                        catalogEntries.get(updatedService).setUuid(uuid);
                    } else {
                        existingServices.get(APIConstants.MAP_KEY_IGNORED).add(updatedService);
                        existingServices.get(APIConstants.MAP_KEY_VERIFIED).remove(updatedService);
                    }
                }
            }

            serviceStatusList = DataMappingUtil.responsePayloadListBuilder(catalogEntries, existingServices, newServices);
            return Response.ok().entity(DataMappingUtil.responsePayloadBuilder(serviceStatusList)).build();
        } else if (overwrite) {
            newResourcesHash = Md5HashGenerator.generateHash(tempDirPath);
            catalogEntries = DataMappingUtil.fromDirToServiceCatalogInfoMap(tempDirPath);
            HashMap<String, ServiceCatalogInfo> serviceEntries = new HashMap<>();
            for (Map.Entry<String,ServiceCatalogInfo> entry : catalogEntries.entrySet()) {
                String key = entry.getKey();
                catalogEntries.get(key).setMd5(newResourcesHash.get(key));
                String uuid = serviceCatalog.addService(catalogEntries.get(key), tenantId);
                if (uuid != null) {
                    catalogEntries.get(key).setUuid(uuid);
                } else {
                    serviceCatalog.updateService(catalogEntries.get(key), tenantId);
                    serviceEntries.put(key, entry.getValue());
                }
            }
            serviceStatusList = DataMappingUtil.updateResponsePayloadListBuilder(serviceEntries);
            return Response.ok().entity(DataMappingUtil.responsePayloadBuilder(serviceStatusList)).build();
        } else {
            return Response.status(Response.Status.CONFLICT).build();
        }
    }

    @Override
    public Response searchServices(String name, String version, String definitionType, String displayName, String key, Boolean shrink, String sortBy, String sortOrder, Integer limit, Integer offset, MessageContext messageContext) throws APIManagementException {
        if (shrink && StringUtils.isBlank(key)) {
            RestApiUtil.handleBadRequest("Service key can not be an empty String with shrink=true", log);
        } else if (shrink && !StringUtils.isBlank(key)) {
            String userName = RestApiCommonUtil.getLoggedInUsername();
            int tenantId = APIUtil.getTenantId(userName);
            List<ServiceInfoDTO> servicesList = new ArrayList<>();

            String keys[] = key.trim().split("\\s*,\\s*");
            for (String serviceKey : keys) {
                ServiceCatalogInfo serviceCatalogInfo = serviceCatalog.getServiceByKey(serviceKey, tenantId);
                if (serviceCatalogInfo != null) {
                    servicesList.add(DataMappingUtil.fromServiceCatalogInfoToServiceInfoDTO(serviceCatalogInfo));
                }
            }
            return Response.ok().entity(DataMappingUtil.getServicesResponsePayloadBuilder(servicesList)).build();
        }
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    public Response updateService(String serviceId, ServiceDTO catalogEntry, InputStream definitionFileInputStream, Attachment definitionFileDetail, MessageContext messageContext) {
        log.info("updateService");

        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }
}
