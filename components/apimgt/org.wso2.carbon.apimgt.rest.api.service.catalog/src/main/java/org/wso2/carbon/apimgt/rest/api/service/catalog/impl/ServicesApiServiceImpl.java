package org.wso2.carbon.apimgt.rest.api.service.catalog.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.api.model.ServiceCatalogEntry;
import org.wso2.carbon.apimgt.impl.ServiceCatalogImpl;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.service.catalog.ServicesApiService;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.VerifierDTO;
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
        return null;
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
        String pathToExportDir =
                System.getProperty(RestApiConstants.JAVA_IO_TMPDIR) + File.separator + ENDPOINT_NAME + DASH + UUID.randomUUID().toString(); //creates a directory in default temporary-file directory
        File file = new File(pathToExportDir);
        file.mkdir();
        String username = RestApiCommonUtil.getLoggedInUsername();
        String exportedFileName = null;
        ExportArchive exportArchive = null;

        if (StringUtils.isBlank(name) || StringUtils.isBlank(version)) {
            RestApiUtil.handleBadRequest("Service name or owner should not be empty or null.", log);
        }

        try {
            consumer = RestApiCommonUtil.getConsumer(username);
            FileBasedServicesImportExportManager importExportManager =
                    new FileBasedServicesImportExportManager(consumer, pathToExportDir);
            exportArchive = importExportManager.createArchiveFromExportedServices(RESOURCE_FOLDER_LOCATION,
                    pathToExportDir, ENDPOINT_NAME);
            exportedServiceArchiveFile = new File(exportArchive.getArchiveName());
            exportedFileName = exportedServiceArchiveFile.getName();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while exporting Services: " + ENDPOINT_NAME, e, log);
        }

        Response.ResponseBuilder responseBuilder =
                Response.status(Response.Status.OK).entity(exportedServiceArchiveFile).type(MediaType.APPLICATION_OCTET_STREAM);
        responseBuilder.header("Content-Disposition", "attachment; filename=\"" + exportedFileName + "\"").header("ETag", exportArchive.getETag());
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
        HashMap<String, Object> validatedServices;
        HashMap<String, Object> newServices;

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

        newResourcesHash = Md5HashGenerator.generateHash(tempDirPath);
        catalogEntries = DataMappingUtil.fromDirToServiceCatalogEntryMap(tempDirPath);
        validatedServices = ServiceCatalogUtils.verifierListValidate(verifier, newResourcesHash, tenantId);
        newServices = ServiceCatalogUtils.filterNewServices(verifier, tenantId);



//        check overwrite value*********************************

        // ***Retrieve endpoint name + version from each metadata and verifier list

        // ***Search each in db with tenant id

        // ***Entries not present in db -> directly to persistence layer

        // ***number of endpoint resources available in zip equals to number of verifier values(for the initial registration of services,
        // can we allow blank?)

        // ***For others go through verifier list and compare each hash with the available values retrieved from db(based on name)

        // ***if hashes are equal, then loop through the list and proceed with CRUD
        // ***new hashes for new content > from map or getHashForEndPoint()

        ///////////////////////////////////////////////////////////////////////////////////
//        if (StringUtils.equals(ifMatch, eTag)) {
//            return Response.status(Response.Status.CONFLICT).build();
//        } else if (overwrite != null) {
//            if (overwrite) {
//                if (Files.notExists(Paths.get(RESOURCE_FOLDER_LOCATION))) {
//                    File rsc = new File(RESOURCE_FOLDER_LOCATION);
//                    rsc.mkdir();
//                }
//                for (File source : fileList) {
//                    try {
//                        FileUtils.copyFile(source, new File(RESOURCE_FOLDER_LOCATION + File.separator + source.getName()));
//                        if (source.getName().equals("metadata.yaml")){
//                            File inputFile = new File(RESOURCE_FOLDER_LOCATION + File.separator + source.getName());
//                            ServiceCatalogInfo serviceCatalogInfo = DataMappingUtil.fromServiceDTOToServiceCatalogInfo(inputFile, eTag);
//                            String uuid = serviceCatalogApi.addServiceCatalog(serviceCatalogInfo,1);
//                            EndPointInfo endPointInfo = DataMappingUtil.generateEndPointInfo(inputFile, uuid);
//                            String id = serviceCatalogApi.addEndPointDefinition(endPointInfo);
//                        }
//                    } catch (IOException e) {
//                        RestApiUtil.handleInternalServerError("Error while updating Service", e, log);
//                    }
//                }
//
//                try {
//                    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
//                    ServiceDTO serviceDTO = mapper.readValue(new File(RESOURCE_FOLDER_LOCATION + File.separator + METADATA_FILE_NAME), ServiceDTO.class);
//                    return Response.ok().header("ETag", eTag).entity(serviceDTO).build();
//                } catch (IOException e) {
//                    RestApiUtil.handleInternalServerError("Error while updating Service dto from metadata.yaml", e, log);
//                }
//            } else {
//                return Response.status(Response.Status.CONFLICT).build();
//            }
//        }
        return null;
    }

//    @Override
//    public Response importService(String serviceId, String ifMatch, Boolean overwrite, List<VerifierDTO> verifier, MessageContext messageContext) throws APIManagementException {
//        String userName = RestApiCommonUtil.getLoggedInUsername();
//        int tenantId = APIUtil.getTenantId(userName);
//        List<String> verifiedKeys;
//        verifiedKeys = ServiceCatalogUtils.VerifierListValidate(verifier, tenantId);
//        return null;
//    }

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
