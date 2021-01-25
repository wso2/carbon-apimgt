package org.wso2.carbon.apimgt.rest.api.service.catalog.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.api.model.ServiceEntry;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.ServiceCatalogImpl;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.service.catalog.ServiceEntriesApiService;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.*;
import org.wso2.carbon.apimgt.rest.api.service.catalog.model.ExportArchive;
import org.wso2.carbon.apimgt.rest.api.service.catalog.utils.ServiceEntryMappingUtil;
import org.wso2.carbon.apimgt.rest.api.service.catalog.utils.FileBasedServicesImportExportManager;
import org.wso2.carbon.apimgt.rest.api.service.catalog.utils.Md5HashGenerator;
import org.wso2.carbon.apimgt.rest.api.service.catalog.utils.ServiceCatalogUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.InputStream;
import java.util.*;


public class ServiceEntriesApiServiceImpl implements ServiceEntriesApiService {

    private static final Log log = LogFactory.getLog(ServiceEntriesApiServiceImpl.class);
    private static final ServiceCatalogImpl serviceCatalog = new ServiceCatalogImpl();

//    @Override
//    public Response checkServicesExistence(String key, Boolean shrink, MessageContext messageContext) throws APIManagementException {
//        if (StringUtils.isBlank(key)) {
//            RestApiUtil.handleBadRequest("Service key can not be an empty String", log);
//        }
//
//        String userName = RestApiCommonUtil.getLoggedInUsername();
//        int tenantId = APIUtil.getTenantId(userName);
//        List<ServiceInfoDTO> servicesList = new ArrayList<>();
//        PaginationDTO paginationDTO = null;
//
//        String keys[] = key.trim().split("\\s*,\\s*");
//        for (String serviceKey : keys) {
//            ServiceCatalogInfo serviceCatalogInfo = serviceCatalog.getServiceByKey(serviceKey, tenantId);
//            if (serviceCatalogInfo != null) {
//                servicesList.add(DataMappingUtil.fromServiceCatalogInfoToServiceInfoDTO(serviceCatalogInfo));
//            }
//        }
//        return Response.ok().entity(DataMappingUtil.getServicesResponsePayloadBuilder(servicesList)).build();// set paginationDTO
//    }

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
        File exportedServiceArchiveFile = null;
        String pathToExportDir = FileBasedServicesImportExportManager.directoryCreator(RestApiConstants.JAVA_IO_TMPDIR); //creates a directory in default temporary-file directory
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
                exportArchive = importExportManager.createArchiveFromExportedServices(ServiceEntryMappingUtil.generateServiceFiles(serviceEntry),
                        pathToExportDir, archiveName);
                exportedServiceArchiveFile = new File(exportArchive.getArchiveName());
                exportedFileName = exportedServiceArchiveFile.getName();
            } else {
                return Response.ok("Empty result set").build(); //404**************
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
    public Response importService(InputStream fileInputStream, Attachment fileDetail, Boolean overwrite, String verifier, MessageContext messageContext) throws APIManagementException {
        String userName = RestApiCommonUtil.getLoggedInUsername();
        int tenantId = APIUtil.getTenantId(userName);
        String tempDirPath = FileBasedServicesImportExportManager.directoryCreator(RestApiConstants.JAVA_IO_TMPDIR);
        List<VerifierDTO> verifierJSONList;
        List<ServiceInfoDTO> serviceStatusList;
        HashMap<String, String> newResourcesHash;
        HashMap<String, ServiceEntry> catalogEntries;
        HashMap<String, List<String>> filteredServices;

        // unzip the uploaded zip
        try {
            FileBasedServicesImportExportManager importExportManager =
                    new FileBasedServicesImportExportManager(tempDirPath);
            importExportManager.importService(fileInputStream);
        } catch (APIMgtResourceAlreadyExistsException e) {
            RestApiUtil.handleResourceAlreadyExistsError("Error while importing Service", e, log);
        }

        if (overwrite == null || !overwrite) {
            if (StringUtils.isBlank(verifier)) {
                RestApiUtil.handleBadRequest("When overwrite is not true, verifier cannot be empty.", log);
            }
            // String to JSON conversion
            verifierJSONList = ServiceEntryMappingUtil.fromStringToJSON(verifier);
            if (verifierJSONList != null && verifierJSONList.size() != ServiceEntryMappingUtil.dirCount(tempDirPath)) {
                RestApiUtil.handleBadRequest("Number of elements in verifier must equals to number of " +
                        "directories in the zip archive.", log);
            }
//OAS Validation
            newResourcesHash = Md5HashGenerator.generateHash(tempDirPath);
            catalogEntries = ServiceEntryMappingUtil.fromDirToServiceCatalogInfoMap(tempDirPath);

            filteredServices = ServiceCatalogUtils.validateVerifierList(verifierJSONList, newResourcesHash, tenantId);
            if (!filteredServices.get(APIConstants.MAP_KEY_IGNORED_EXISTING_SERVICE).isEmpty()){
                return Response.status(Response.Status.PRECONDITION_FAILED).build(); // RESPONSE BODY?
            }

            // *********these 2 will be moved to ServiceCatalogUtils
            // Adding new services
            List<String> keyList = filteredServices.get(APIConstants.MAP_KEY_ACCEPTED_NEW_SERVICE);
            for (String newService : keyList) {
                if (catalogEntries.containsKey(newService)) { // we can remove this since in two flows we use same key
                    catalogEntries.get(newService).setMd5(newResourcesHash.get(newService));
                    String uuid = serviceCatalog.addService(catalogEntries.get(newService), tenantId);
                    if (uuid != null) {
                        catalogEntries.get(newService).setUuid(uuid); // no need any more
                        // get values from db.. not from user
                    } else {
                        filteredServices.get(APIConstants.MAP_KEY_IGNORED_EXISTING_SERVICE).add(newService);
                        filteredServices.get(APIConstants.MAP_KEY_ACCEPTED_NEW_SERVICE).remove(newService);
                    }
                }
            }

            // Adding updated existing services
            for (String updatedService : filteredServices.get(APIConstants.MAP_KEY_VERIFIED_EXISTING_SERVICE)) {
                if (catalogEntries.containsKey(updatedService)) { // as above
                    catalogEntries.get(updatedService).setMd5(newResourcesHash.get(updatedService)); // validation will come here
                    String uuid = serviceCatalog.addService(catalogEntries.get(updatedService), tenantId);
                    if (uuid != null) {
                        catalogEntries.get(updatedService).setUuid(uuid);
                    } else {
                        filteredServices.get(APIConstants.MAP_KEY_IGNORED_EXISTING_SERVICE).add(updatedService);
                        filteredServices.get(APIConstants.MAP_KEY_VERIFIED_EXISTING_SERVICE).remove(updatedService);
                    }
                }
            }

            serviceStatusList = ServiceEntryMappingUtil.fromServiceCatalogInfoToDTOList(catalogEntries, filteredServices);
            return Response.ok().entity(ServiceEntryMappingUtil.fromServiceInfoDTOToServiceInfoListDTO(serviceStatusList)).build();
        } else if (overwrite) {
            newResourcesHash = Md5HashGenerator.generateHash(tempDirPath); // take this out from if-else
            catalogEntries = ServiceEntryMappingUtil.fromDirToServiceCatalogInfoMap(tempDirPath); // take out
            HashMap<String, ServiceEntry> serviceEntries = new HashMap<>();
            for (Map.Entry<String, ServiceEntry> entry : catalogEntries.entrySet()) {
                String key = entry.getKey();
                catalogEntries.get(key).setMd5(newResourcesHash.get(key));
                String uuid = serviceCatalog.addService(catalogEntries.get(key), tenantId); // before this there will be validation whether or not update
//Use try catch
                if (uuid != null) {
                    catalogEntries.get(key).setUuid(uuid);
                    serviceEntries.put(key, entry.getValue());
                } else {
                    String ServiceKey = serviceCatalog.updateService(catalogEntries.get(key), tenantId); // do the validation here - remove from add
                    if (!StringUtils.isBlank(ServiceKey))
                        serviceEntries.put(key, entry.getValue());
                }
            }
            serviceStatusList = ServiceEntryMappingUtil.fromServiceCatalogInfoToDTOList(serviceEntries);
            return Response.ok().entity(ServiceEntryMappingUtil.fromServiceInfoDTOToServiceInfoListDTO(serviceStatusList)).build();
        } else {
            return Response.status(Response.Status.CONFLICT).build(); // remove this and keep only if and else
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
                ServiceEntry serviceEntry = serviceCatalog.getServiceByKey(serviceKey, tenantId);
                if (serviceEntry != null) {
                    servicesList.add(ServiceEntryMappingUtil.fromServiceCatalogInfoToServiceInfoDTO(serviceEntry));
                }
            }
            return Response.ok().entity(ServiceEntryMappingUtil.getServicesResponsePayloadBuilder(servicesList)).build();
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
