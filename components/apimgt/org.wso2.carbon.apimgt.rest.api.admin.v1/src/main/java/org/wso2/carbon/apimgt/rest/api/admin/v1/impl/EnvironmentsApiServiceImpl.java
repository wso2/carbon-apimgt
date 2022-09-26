package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.rest.api.admin.v1.EnvironmentsApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.impl.EnvironmentsCommonImpl;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.EnvironmentDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.EnvironmentListDTO;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;


public class EnvironmentsApiServiceImpl implements EnvironmentsApiService {


    /**
     * Delete gateway environment
     *
     * @param environmentId  environment ID
     * @param messageContext message context
     * @return 200 with empty response body
     * @throws APIManagementException if failed to delete
     */
    public Response removeEnvironment(String environmentId, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        EnvironmentsCommonImpl.removeEnvironment(environmentId, organization);
        return Response.ok().build();
    }

    /**
     * Update gateway environment
     *
     * @param environmentId  environment ID
     * @param body           environment to be updated
     * @param messageContext message context
     * @return updated environment
     * @throws APIManagementException if failed to update
     */
    public Response updateEnvironment(String environmentId, EnvironmentDTO body, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        EnvironmentsCommonImpl.updateEnvironment(environmentId, body, organization);
        try {
            URI location = new URI(RestApiConstants.RESOURCE_PATH_ENVIRONMENT
                    + RestApiConstants.PATH_DELIMITER + environmentId);
            return Response.ok(location).entity(body).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while updating Environment : " + environmentId;
            throw new APIManagementException(errorMessage, ExceptionCodes.INTERNAL_ERROR);
        }
    }

    /**
     * Get list of gateway environments from config api-manager.xml and dynamic environments (from DB)
     *
     * @param messageContext message context
     * @return created environment
     * @throws APIManagementException if failed to get list
     */
    public Response getEnvironments(MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        EnvironmentListDTO envListDTO = EnvironmentsCommonImpl.getEnvironments(organization);
        return Response.ok().entity(envListDTO).build();
    }

    /**
     * Create a dynamic gateway environment
     *
     * @param body           environment to be created
     * @param messageContext message context
     * @return created environment
     * @throws APIManagementException if failed to create
     */
    public Response addEnvironment(EnvironmentDTO body, MessageContext messageContext) throws APIManagementException {
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            EnvironmentDTO envDTO = EnvironmentsCommonImpl.addEnvironment(body, organization);
            URI location = new URI(RestApiConstants.RESOURCE_PATH_ENVIRONMENT
                    + RestApiConstants.PATH_DELIMITER + envDTO.getId());
            return Response.created(location).entity(envDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while adding gateway environment : " + body.getName() + "-" + e.getMessage();
            throw new APIManagementException(errorMessage, ExceptionCodes.INTERNAL_ERROR);
        }
    }
}
