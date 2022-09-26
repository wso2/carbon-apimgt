package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.rest.api.admin.v1.KeyManagersApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.impl.KeyManagerCommonImpl;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerWellKnownResponseDTO;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;

public class KeyManagersApiServiceImpl implements KeyManagersApiService {

    @Override
    public Response getWellKnownInfoKeyManager(String url, String type, MessageContext messageContext)
            throws APIManagementException {
        KeyManagerWellKnownResponseDTO keyManagerWellKnownResponseDTO
                = KeyManagerCommonImpl.getWellKnownInfoKeyManager(url, type);
        return Response.ok().entity(keyManagerWellKnownResponseDTO).build();
    }

    public Response getAllKeyManagers(MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getOrganization(messageContext);
        KeyManagerListDTO keyManagerListDTO = KeyManagerCommonImpl.getAllKeyManagers(organization);
        return Response.ok().entity(keyManagerListDTO).build();
    }

    public Response removeKeyManager(String keyManagerId, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getOrganization(messageContext);
        KeyManagerCommonImpl.removeKeyManager(keyManagerId, organization);
        return Response.ok().build();
    }

    public Response getKeyManagerConfiguration(String keyManagerId, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getOrganization(messageContext);
        KeyManagerDTO keyManagerDTO = KeyManagerCommonImpl.getKeyManagerConfiguration(keyManagerId, organization);
        return Response.ok(keyManagerDTO).build();
    }

    public Response updateKeyManager(String keyManagerId, KeyManagerDTO body, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getOrganization(messageContext);
        KeyManagerDTO keyManagerDTO = KeyManagerCommonImpl.updateKeyManager(keyManagerId, body, organization);
        return Response.ok(keyManagerDTO).build();
    }

    public Response addNewKeyManager(KeyManagerDTO body, MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getOrganization(messageContext);
        try {
            KeyManagerDTO keyManagerDTO = KeyManagerCommonImpl.addNewKeyManager(body, organization);
            URI location = new URI(RestApiConstants.KEY_MANAGERS + RestApiConstants.PATH_DELIMITER
                    + keyManagerDTO.getId());
            return Response.created(location).entity(keyManagerDTO).build();
        } catch (URISyntaxException e) {
            String error = "Error while Creating Key Manager configuration in organization " + organization;
            throw new APIManagementException(error, e, ExceptionCodes.INTERNAL_ERROR);
        }
    }
}
