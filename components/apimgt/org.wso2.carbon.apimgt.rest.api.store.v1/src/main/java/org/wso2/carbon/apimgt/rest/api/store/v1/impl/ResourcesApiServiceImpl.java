package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.ResourcesApiService;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class ResourcesApiServiceImpl implements ResourcesApiService {

    private static final Set<String> ALLOWED_RESOURCE_TYPES = new HashSet<>();
    static {
        ALLOWED_RESOURCE_TYPES.add("wsdl");
        ALLOWED_RESOURCE_TYPES.add("swagger");
        ALLOWED_RESOURCE_TYPES.add("openapi");
    }

    /**
     * Generate a URL to download a resource for an API
     *
     * @param resourceType    Type of the resource (wsdl, swagger, openapi)
     * @param apiId           API identifier
     * @param environmentName  Name of the API gateway environment.
     * @param messageContext  Message context for the request
     * @return Response containing the generated download URL or an error message
     * @throws APIManagementException if an error occurs while generating the URL
     */
    public Response generateUrlToDownloadResource(String resourceType, String apiId, String environmentName, MessageContext messageContext)
            throws APIManagementException {

        if (StringUtils.isEmpty(apiId) || StringUtils.isEmpty(resourceType)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Collections.singletonMap("error", "Resource Type is required")).build();
        }

        if (!ALLOWED_RESOURCE_TYPES.contains(resourceType.toLowerCase())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Collections.singletonMap("error", "Unsupported resource Type")).build();
        }

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
        String generatedUrl = null;
        if (resourceType.equals("wsdl")) {
            generatedUrl = apiConsumer.generateUrlToWSDL(apiId, resourceType, organization, environmentName);
        }
        Map<String, String> resp = Collections.singletonMap("url", generatedUrl);
        return Response.ok(resp).build();
    }
}