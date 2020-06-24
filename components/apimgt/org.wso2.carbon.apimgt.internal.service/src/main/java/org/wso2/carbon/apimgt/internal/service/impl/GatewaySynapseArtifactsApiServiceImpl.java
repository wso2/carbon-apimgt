package org.wso2.carbon.apimgt.internal.service.impl;

import com.google.common.io.ByteStreams;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class GatewaySynapseArtifactsApiServiceImpl implements GatewaySynapseArtifactsApiService {

    protected ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

    public Response gatewaySynapseArtifactsGet(String gatewayLabel, MessageContext messageContext) throws APIManagementException {

        List<String> gatewayRuntimeArtifactsArray = new ArrayList<>();
        try {
            List<ByteArrayInputStream> baip = apiMgtDAO.getAllGatewayPublishedAPIArtifacts(gatewayLabel);
            for (ByteArrayInputStream byteStream :baip){
                byte[] bytes = ByteStreams.toByteArray(byteStream);
                String  gatewayRuntimeArtifacts = new String(bytes);
                gatewayRuntimeArtifactsArray.add(gatewayRuntimeArtifacts);
            }
        } catch (APIManagementException | IOException e) {
            throw new APIManagementException("Error retrieving Artifact from DB", e);
        }
        return Response.ok().entity(gatewayRuntimeArtifactsArray).build();
    }
}
