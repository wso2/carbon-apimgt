package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.CompositeAPI;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.ETagUtils;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.CompositeApisApiService;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.dto.CompositeAPIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.CompositeAPIListDTO;
import org.wso2.carbon.apimgt.rest.api.store.mappings.CompositeAPIMappingUtil;
import org.wso2.msf4j.Request;

import java.util.HashMap;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date =
        "2017-04-03T17:36:56.084+05:30")
public class CompositeApisApiServiceImpl extends CompositeApisApiService {
    private static final Logger log = LoggerFactory.getLogger(ApisApiServiceImpl.class);

    /**
     * Deletes a particular API
     *
     * @param apiId             UUID of API
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return 200 OK if the opration was successful
     * @throws NotFoundException when the particular resource does not exist
     */
    @Override
    public Response compositeApisApiIdDelete(String apiId, String ifMatch, String ifUnmodifiedSince, Request request)
            throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiStore = RestApiUtil.getConsumer(username);
            String existingFingerprint = compositeApisApiIdDeleteFingerprint(apiId, ifMatch,
                                                                            ifUnmodifiedSince, request);
            if (!StringUtils.isEmpty(ifMatch) && !StringUtils.isEmpty(existingFingerprint) && !ifMatch
                    .contains(existingFingerprint)) {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }

            apiStore.deleteCompositeApi(apiId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Retrives an API by UUID
     *
     * @param apiId           UUID of API
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return API which is identified by the given UUID
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response compositeApisApiIdGet(String apiId, String accept, String ifNoneMatch,
                                          String ifModifiedSince, Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            if (!RestApiUtil.getConsumer(username).checkIfAPIExists(apiId)) {
                String errorMessage = "API not found : " + apiId;
                APIMgtResourceNotFoundException e = new APIMgtResourceNotFoundException(errorMessage,
                        ExceptionCodes.API_NOT_FOUND);
                HashMap<String, String> paramList = new HashMap<String, String>();
                paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
                return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
            }

            String existingFingerprint = compositeApisApiIdGetFingerprint(apiId, accept, ifNoneMatch, ifModifiedSince,
                    request);
            if (!StringUtils.isEmpty(ifNoneMatch) && !StringUtils.isEmpty(existingFingerprint) && ifNoneMatch
                    .contains(existingFingerprint)) {
                return Response.notModified().build();
            }

            CompositeAPIDTO apidto = CompositeAPIMappingUtil.toCompositeAPIDTO(RestApiUtil.getConsumer(username).
                    getCompositeAPIbyId(apiId));
            return Response.ok().header(HttpHeaders.ETAG, "\"" + existingFingerprint + "\"")
                                                                            .entity(apidto).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API : " + apiId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Updates an API by UUID
     *
     * @param apiId             UUID of API
     * @param body              Updated API details
     * @param contentType       Content-Type header value
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Updated API
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response compositeApisApiIdPut(String apiId, CompositeAPIDTO body, String contentType, String ifMatch,
                                          String ifUnmodifiedSince, Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiStore = RestApiUtil.getConsumer(username);
            String existingFingerprint = compositeApisApiIdGetFingerprint(apiId, null, null, null, request);
            if (!StringUtils.isEmpty(ifMatch) && !StringUtils.isEmpty(existingFingerprint) && !ifMatch
                    .contains(existingFingerprint)) {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }

            CompositeAPI.Builder api = CompositeAPIMappingUtil.toAPI(body).id(apiId);
            apiStore.updateCompositeApi(api);

            String newFingerprint = compositeApisApiIdGetFingerprint(apiId, null, null, null, request);
            CompositeAPIDTO apidto = CompositeAPIMappingUtil.toCompositeAPIDTO(apiStore.getCompositeAPIbyId(apiId));
            return Response.ok().header(HttpHeaders.ETAG, "\"" + newFingerprint + "\"").entity(apidto).build();
        } catch (APIManagementException e) {
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Retrieves the swagger definition of an API
     *
     * @param apiId           UUID of API
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return swagger definition of an API
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response compositeApisApiIdSwaggerGet(String apiId, String accept, String ifNoneMatch,
                                                 String ifModifiedSince, Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiStore = RestApiUtil.getConsumer(username);
            String existingFingerprint = compositeApisApiIdSwaggerGetFingerprint(apiId, accept, ifNoneMatch,
                    ifModifiedSince, request);
            if (!StringUtils.isEmpty(ifNoneMatch) && !StringUtils.isEmpty(existingFingerprint) && ifNoneMatch
                    .contains(existingFingerprint)) {
                return Response.notModified().build();
            }
            String swagger = apiStore.getSwagger20Definition(apiId);
            return Response.ok().header(HttpHeaders.ETAG, "\"" + existingFingerprint + "\"").entity(swagger).build();
        } catch (APIManagementException e) {
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Updates the swagger defnition of an API
     *
     * @param apiId             UUID of API
     * @param apiDefinition     updated swagger defintion
     * @param contentType       Content-Type header value
     * @param ifMatch           If-Match header value
     * @param ifUnmodifiedSince If-Unmodified-Since header value
     * @param request           msf4j request object
     * @return Updated swagger definition
     * @throws NotFoundException When the particular resource does not exist in the system
     */
    @Override
    public Response compositeApisApiIdSwaggerPut(String apiId, String apiDefinition, String contentType, String ifMatch,
                                                 String ifUnmodifiedSince, Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiStore = RestApiUtil.getConsumer(username);
            String existingFingerprint = compositeApisApiIdSwaggerGetFingerprint(apiId, null, null, null, request);
            if (!StringUtils.isEmpty(ifMatch) && !StringUtils.isEmpty(existingFingerprint) && !ifMatch
                    .contains(existingFingerprint)) {
                return Response.status(Response.Status.PRECONDITION_FAILED).build();
            }
            apiStore.saveSwagger20Definition(apiId, apiDefinition);
            String apiSwagger = apiStore.getSwagger20Definition(apiId);
            String newFingerprint = compositeApisApiIdSwaggerGetFingerprint(apiId, null, null, null, request);
            return Response.ok().header(HttpHeaders.ETAG, "\"" + newFingerprint + "\"").entity(apiSwagger).build();
        } catch (APIManagementException e) {
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    @Override
    public Response compositeApisGet(Integer limit, Integer offset, String query, String accept, String ifNoneMatch,
                                     Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        CompositeAPIListDTO apiListDTO = null;
        try {
            apiListDTO = CompositeAPIMappingUtil.toCompositeAPIListDTO(RestApiUtil.getConsumer(username).
                    searchCompositeAPIs(query, offset, limit));
            return Response.ok().entity(apiListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving APIs";
            HashMap<String, String> paramList = new HashMap<String, String>();
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    @Override
    public Response compositeApisPost(CompositeAPIDTO body, String contentType, Request request)
            throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            CompositeAPI.Builder apiBuilder = CompositeAPIMappingUtil.toAPI(body);
            APIStore apiStore = RestApiUtil.getConsumer(username);
            apiStore.addCompositeApi(apiBuilder);
            CompositeAPI returnAPI = apiStore.getCompositeAPIbyId(apiBuilder.build().getId());
            return Response.status(Response.Status.CREATED).
                    entity(CompositeAPIMappingUtil.toCompositeAPIDTO(returnAPI)).build();
        } catch (APIManagementException e) {
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_NAME, body.getName());
            paramList.put(APIMgtConstants.ExceptionsConstants.API_VERSION, body.getVersion());
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }

    }

    private String compositeApisApiIdDeleteFingerprint(String apiId, String ifMatch, String ifUnmodifiedSince,
                                                       Request request) {
        return getEtag(apiId);
    }

    private String compositeApisApiIdGetFingerprint(String apiId, String accept, String ifNoneMatch,
                          String ifModifiedSince, Request request) {
        return getEtag(apiId);
    }

    private String compositeApisApiIdSwaggerGetFingerprint(String apiId, String accept, String ifNoneMatch,
                                                String ifModifiedSince, Request request) {
        return getEtag(apiId);
    }

    /**
     * Retrieves last updatedtime for an API given the api id
     *
     * @param apiId API ID
     * @return Last updated time
     */
    private String getEtag(String apiId){
        String username = RestApiUtil.getLoggedInUsername();
        String eTag = "";
        try {
            String lastUpdatedTime = RestApiUtil.getConsumer(username).getLastUpdatedTimeOfAPI(apiId);
            eTag = ETagUtils.generateETag(lastUpdatedTime);
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving last updated time of API " + apiId;
            log.error(errorMessage, e);
        }

        return eTag;
    }
}
