package org.wso2.carbon.apimgt.rest.api.service.catalog.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.service.catalog.ServicesApiService;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.model.ExportArchive;
import org.wso2.carbon.apimgt.rest.api.service.catalog.utils.ETagValueGenerator;
import org.wso2.carbon.apimgt.rest.api.service.catalog.utils.FileBasedServicesImportExportManager;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class ServicesApiServiceImpl implements ServicesApiService {

    private static final Log log = LogFactory.getLog(ServicesApiServiceImpl.class);
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
                String eTag = ETagValueGenerator.getETag(fileList);

                return Response.ok().header("ETag", eTag).build();
            } else {
                RestApiUtil.handleBadRequest("Invalid service name or version defined", log);
            }
        }
        return Response.status(Response.Status.NOT_FOUND).build();
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
    public Response importService(String serviceId, InputStream fileInputStream, Attachment fileDetail, String ifMatch,
                                  Boolean overwrite, MessageContext messageContext) throws APIManagementException {
        if (StringUtils.isBlank(ifMatch)) {
            ifMatch = null;
        }
        APIConsumer consumer;
        String username = RestApiCommonUtil.getLoggedInUsername();
        String tempDirPath = System.getProperty(RestApiConstants.JAVA_IO_TMPDIR) + File.separator + ENDPOINT_NAME + DASH
                + UUID.randomUUID().toString();
        File file = new File(tempDirPath);
        file.mkdir();
        String currentETag = null;
        if (Files.exists(Paths.get(RESOURCE_FOLDER_LOCATION))) {
            List<File> existingFiles = new ArrayList<File>();
            existingFiles.add(new File(RESOURCE_FOLDER_LOCATION + File.separator + METADATA_FILE_NAME));
            existingFiles.add(new File(RESOURCE_FOLDER_LOCATION + File.separator + OAS_FILE_NAME));
            currentETag = ETagValueGenerator.getETag(existingFiles);
        }

        if (!StringUtils.equals(currentETag, ifMatch)) {
            return Response.status(Response.Status.PRECONDITION_FAILED).build();
        }
        try {
            consumer = RestApiCommonUtil.getConsumer(username);
            FileBasedServicesImportExportManager importExportManager =
                    new FileBasedServicesImportExportManager(consumer, tempDirPath);
            importExportManager.importService(fileInputStream);
        } catch (APIMgtResourceAlreadyExistsException e) {
            RestApiUtil.handleResourceAlreadyExistsError("Error while importing Service", e, log);
        }

        List<File> fileList = new ArrayList<File>();
        fileList.add(new File(tempDirPath + File.separator + METADATA_FILE_NAME));
        fileList.add(new File(tempDirPath + File.separator + OAS_FILE_NAME));
        String eTag = ETagValueGenerator.getETag(fileList);

        if (StringUtils.equals(ifMatch, eTag)) {
            return Response.status(Response.Status.CONFLICT).build();
        } else if (overwrite != null) {
            if (overwrite) {
                if (Files.notExists(Paths.get(RESOURCE_FOLDER_LOCATION))) {
                    File rsc = new File(RESOURCE_FOLDER_LOCATION);
                    rsc.mkdir();
                }
                for (File source : fileList) {
                    try {
                        FileUtils.copyFile(source, new File(RESOURCE_FOLDER_LOCATION + File.separator + source.getName()));
                    } catch (IOException e) {
                        RestApiUtil.handleInternalServerError("Error while updating Service", e, log);
                    }
                }
                try {
                    ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
                    ServiceDTO serviceDTO = mapper.readValue(new File(RESOURCE_FOLDER_LOCATION + File.separator + METADATA_FILE_NAME), ServiceDTO.class);
                    return Response.ok().header("ETag", eTag).entity(serviceDTO).build();
                } catch (IOException e) {
                    RestApiUtil.handleInternalServerError("Error while updating Service dto from metadata.yaml", e, log);
                }
            } else {
                return Response.status(Response.Status.CONFLICT).build();
            }
        }
        return null;
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
