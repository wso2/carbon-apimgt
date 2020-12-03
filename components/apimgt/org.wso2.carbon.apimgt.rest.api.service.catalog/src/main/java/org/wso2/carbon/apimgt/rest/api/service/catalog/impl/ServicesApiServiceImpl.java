package org.wso2.carbon.apimgt.rest.api.service.catalog.impl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.rest.api.service.catalog.ServicesApiService;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.model.ExportArchive;
import org.wso2.carbon.apimgt.rest.api.service.catalog.model.Service;
import org.wso2.carbon.apimgt.rest.api.service.catalog.utils.ETagValueGenerator;
import org.wso2.carbon.apimgt.rest.api.service.catalog.utils.FileBasedServicesImportExportManager;
import org.wso2.carbon.apimgt.rest.api.service.catalog.utils.mappings.ServiceMapping;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
        if (StringUtils.equals(name, ENDPOINT_NAME) && StringUtils.equals(version, ENDPOINT_VERSION)) {
            List<File> fileList = new ArrayList<File>();
            fileList.add(new File(RESOURCE_FOLDER_LOCATION + File.separator + METADATA_FILE_NAME));
            fileList.add(new File(RESOURCE_FOLDER_LOCATION + File.separator + OAS_FILE_NAME));
            String eTag = ETagValueGenerator.getETag(fileList);

            return Response.ok().header("ETag", eTag).build();
        } else {
            RestApiUtil.handleBadRequest("Invalid API Category name(s) defined", log);
        }
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
        String exportedFilePath;
        File exportedServiceArchiveFile = null;
        String pathToExportDir =
                System.getProperty(RestApiConstants.JAVA_IO_TMPDIR) + File.separator + ENDPOINT_NAME + DASH + UUID.randomUUID().toString(); //creates a directory in default temporary-file directory
        //Creating a File object
        File file = new File(pathToExportDir);
        //Creating the directory
        boolean bool = file.mkdir();
        String username = RestApiUtil.getLoggedInUsername();
        String exportedFileName = null;
        ExportArchive exportArchive = null;

        if (StringUtils.isBlank(name) || StringUtils.isBlank(version)) {
            RestApiUtil.handleBadRequest("Service name or owner should not be empty or null.", log);
        }

        try {
            consumer = RestApiUtil.getConsumer(username);
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
    public Response importService(String serviceId, InputStream fileInputStream, Attachment fileDetail, String ifMatch, Boolean overwrite, MessageContext messageContext) throws APIManagementException {
        APIConsumer consumer;
        String username = RestApiUtil.getLoggedInUsername();
        String tempDirPath = System.getProperty(RestApiConstants.JAVA_IO_TMPDIR) + File.separator + ENDPOINT_NAME + DASH + UUID.randomUUID().toString(); //creates a directory in default temporary-file directory
        //Creating a File object
        File file = new File(tempDirPath);
        //Creating the directory
        boolean bool = file.mkdir();
        try {
            consumer = RestApiUtil.getConsumer(username);
            FileBasedServicesImportExportManager importExportManager =
                    new FileBasedServicesImportExportManager(consumer, tempDirPath);
            importExportManager.importService(fileInputStream);
        } catch (APIMgtResourceAlreadyExistsException e) {
            RestApiUtil.handleResourceAlreadyExistsError("Error while importing Service", e, log);
        }

        if (!StringUtils.isBlank(ifMatch)) {
            List<File> fileList = new ArrayList<File>();
            fileList.add(new File(tempDirPath + File.separator + METADATA_FILE_NAME));
            fileList.add(new File(tempDirPath + File.separator + OAS_FILE_NAME));
            String eTag = ETagValueGenerator.getETag(fileList);

            if (StringUtils.equals(ifMatch, eTag)) {
                return Response.notModified().build();
            } else if (overwrite != null) {
                if (overwrite) {
                    for (File source : fileList) {
                        try {
                            FileUtils.copyFile(source, new File(RESOURCE_FOLDER_LOCATION + File.separator + source.getName()));
                        } catch (IOException e) {
                            RestApiUtil.handleInternalServerError("Error while updating Service", e, log);
                        }
                    }
                    ServiceDTO serviceDTO;
                    Service service = new Service();
                    service.setId("01234567-0123-0123-0123-012345678901");
                    service.setName("Swagger Petstore");
                    service.setDisplayName("Swagger Petstore");
                    service.setDescription("This is a sample server Petstore server.  You can find out more about     Swagger at [http://swagger.io](http://swagger.io) or on [irc.freenode.net, #swagger](http://swagger.io/irc/).      For this sample, you can use the api key `special-key` to test the authorization     filters.");
                    service.setVersion("1.0.0");
                    service.setServiceUrl("http://swagger.io");

                    serviceDTO = ServiceMapping.fromServiceToDTO(service);
                    return Response.ok().header("ETag", eTag).entity(serviceDTO).build();
                } else {
                    return Response.notModified().build();
                }
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
