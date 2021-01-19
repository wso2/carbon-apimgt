package org.wso2.carbon.apimgt.rest.api.service.catalog.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.api.model.EndPointInfo;
import org.wso2.carbon.apimgt.api.model.ServiceCatalogEntry;
import org.wso2.carbon.apimgt.api.model.ServiceCatalogInfo;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.ServiceCatalogImpl;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.service.catalog.ServicesApiService;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class ServicesApiServiceImpl implements ServicesApiService {

    private static final Log log = LogFactory.getLog(ServicesApiServiceImpl.class);
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
        return Response.ok().entity(DataMappingUtil.StatusResponsePayloadBuilder(servicesList, paginationDTO)).build();// set paginationDTO
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
        EndPointInfo endPointInfo;
        String exportedFileName = null;
        ExportArchive exportArchive;

        if (StringUtils.isBlank(name) || StringUtils.isBlank(version)) {
            RestApiUtil.handleBadRequest("Service name or owner should not be empty or null.", log);
        }

        try {
            endPointInfo = serviceCatalog.getEndPointResourcesByNameAndVersion(name, version, tenantId);
            if (endPointInfo != null) {
                consumer = RestApiCommonUtil.getConsumer(userName);
                FileBasedServicesImportExportManager importExportManager =
                        new FileBasedServicesImportExportManager(consumer, pathToExportDir);
                exportArchive = importExportManager.createArchiveFromExportedServices(DataMappingUtil.fromEndPointInfoToFiles(endPointInfo),
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
    public Response importService(String serviceId, InputStream fileInputStream, Attachment fileDetail,
                                  List<VerifierDTO> verifier, String ifMatch, Boolean overwrite, MessageContext messageContext)
            throws APIManagementException {
        APIConsumer consumer;
        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        String tempDirPath = FileBasedServicesImportExportManager.directoryCreator(RestApiConstants.JAVA_IO_TMPDIR);
        HashMap<String, String> newResourcesHash;
        HashMap<String, ServiceCatalogEntry> catalogEntries;
        HashMap<String, List<String>> existingServices;
        HashMap<String, List<String>> newServices;
        List<ServiceCRUDStatusDTO> serviceStatusList;
        PaginationDTO paginationDTO = null;

        // unzip the uploaded zip
        try {
            consumer = RestApiCommonUtil.getConsumer(userName);
            FileBasedServicesImportExportManager importExportManager =
                    new FileBasedServicesImportExportManager(consumer, tempDirPath);
            importExportManager.importService(fileInputStream);
        } catch (APIMgtResourceAlreadyExistsException e) {
            RestApiUtil.handleResourceAlreadyExistsError("Error while importing Service", e, log);
        }

        if (verifier.size() != DataMappingUtil.dirCount(tempDirPath)) {
            RestApiUtil.handleBadRequest("Number of elements in verifier must equals to number of directories in the zip archive.", log);
        }
        if (overwrite) {
            newResourcesHash = Md5HashGenerator.generateHash(tempDirPath);
            catalogEntries = DataMappingUtil.fromDirToServiceCatalogEntryMap(tempDirPath);
            existingServices = ServiceCatalogUtils.verifierListValidate(verifier, newResourcesHash, tenantId);
            newServices = ServiceCatalogUtils.filterNewServices(verifier, tenantId);

            // Adding new services
            for (String newService : newServices.get(APIConstants.MAP_KEY_ACCEPTED)) {
                if (catalogEntries.containsKey(newService)) {
                    String uuid = serviceCatalog.addServiceCatalog(catalogEntries.get(newService), tenantId);
                    if (uuid != null) {
                        catalogEntries.get(newService).getServiceCatalogInfo().setUuid(uuid);
                    } else {
                        newServices.get(APIConstants.MAP_KEY_IGNORED).add(newService);
                        newServices.get(APIConstants.MAP_KEY_ACCEPTED).remove(newService);
                    }
                }
            }

            // Adding updated services
            for (String updatedService : existingServices.get(APIConstants.MAP_KEY_VERIFIED)) {
                if (catalogEntries.containsKey(updatedService)) {
                    String uuid = serviceCatalog.addServiceCatalog(catalogEntries.get(updatedService), tenantId);
                    if (uuid != null) {
                        catalogEntries.get(updatedService).getServiceCatalogInfo().setUuid(uuid);
                    } else {
                        existingServices.get(APIConstants.MAP_KEY_IGNORED).add(updatedService);
                        existingServices.get(APIConstants.MAP_KEY_VERIFIED).remove(updatedService);
                    }
                }
            }

            serviceStatusList = DataMappingUtil.responsePayloadListBuilder(catalogEntries, existingServices, newServices);
            return Response.ok().entity(DataMappingUtil.responsePayloadBuilder(serviceStatusList, paginationDTO)).build();// set paginationDTO
        } else {
            return Response.status(Response.Status.CONFLICT).build();
        }
    }

    public Response searchServices(String name, String version, String definitionType, String displayName, String sortBy, String sortOrder, Integer limit, Integer offset, MessageContext messageContext) {
        log.info("searchServices");

        // remove errorObject and add implementation code!
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
