package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.ETagUtils;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.core.NotFoundException;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.HashMap;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-04-26T10:56:28.057+05:30")
public class ApisApiServiceImpl extends ApisApiService {

    private static final Logger log = LoggerFactory.getLogger(ApisApiServiceImpl.class);

    /**
     * Retrieve API's gateway configuration
     *
     * @param apiId   UUID of the API
     * @param request msf4j request object
     * @return 200 OK if the opration was successful
     * @throws NotFoundException If failed to retrieve API gateway configuration
     */
    @Override
    public Response apisApiIdGatewayConfigGet(String apiId, String accept, String ifNoneMatch, String ifModifiedSince,
                                              Request request) throws NotFoundException {

        try {
            APIMgtAdminService apiMgtAdminService = APIManagerFactory.getInstance().getAPIMgtAdminService();
            String apiGatewayConfig = null;
            String existingFingerprint = apisApiIdGatewayConfigGetFingerprint(apiId, accept, ifNoneMatch,
                    ifModifiedSince, request);

            if (!StringUtils.isEmpty(ifNoneMatch) && !StringUtils.isEmpty(existingFingerprint) && ifNoneMatch
                    .contains(existingFingerprint)) {
                return Response.notModified().build();
            }
            
            apiGatewayConfig = apiMgtAdminService.getAPIGatewayServiceConfig(apiId);

            if (apiGatewayConfig != null) {
                return Response.ok().header(HttpHeaders.ETAG, "\"" + existingFingerprint + "\"").entity(apiGatewayConfig)
                        .build();
            } else {
                String msg = "API is not found with apiId : " + apiId;
                ErrorDTO errorDTO = RestApiUtil.getErrorDTO(msg, 900314L, msg);
                log.error(msg);
                return Response.status(Response.Status.NOT_FOUND).entity(errorDTO).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving gateway config of API : " + apiId;
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.API_ID, apiId);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Retrieves the fingerprint of a gateway config provided its API's UUID
     *
     * @param apiId           UUID of API
     * @param accept          Accept header value
     * @param ifNoneMatch     If-None-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param request         msf4j request object
     * @return fingerprint of the gateaway config
     */
    public String apisApiIdGatewayConfigGetFingerprint(String apiId, String accept, String ifNoneMatch,
                                                       String ifModifiedSince, Request request) {

        try {
            APIMgtAdminService apiMgtAdminService = APIManagerFactory.getInstance().getAPIMgtAdminService();
            String lastUpdatedTime = apiMgtAdminService.getLastUpdatedTimeOfGatewayConfig(apiId);
            return ETagUtils.generateETag(lastUpdatedTime);
        } catch (APIManagementException e) {
            //gives a warning and let it continue the execution
            String errorMessage = "Error while retrieving last updated time of gateway config of API " + apiId;
            log.error(errorMessage, e);
            return null;
        }
    }
}
