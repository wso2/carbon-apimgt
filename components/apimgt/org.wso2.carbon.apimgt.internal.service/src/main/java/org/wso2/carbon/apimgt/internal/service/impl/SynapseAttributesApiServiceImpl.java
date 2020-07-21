package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;
import org.wso2.carbon.apimgt.internal.service.dto.SynapseAttributesDTO;

import java.util.ArrayList;
import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class SynapseAttributesApiServiceImpl implements SynapseAttributesApiService {

    private GatewayArtifactsMgtDAO gatewayArtifactsMgtDAO = GatewayArtifactsMgtDAO.getInstance();
    private SynapseAttributesDTO synapseArtifactListDTOS = new SynapseAttributesDTO();
    private static final Log log = LogFactory.getLog(SynapseAttributesApiServiceImpl.class);

    @Override public Response synapseAttributesGet(String apiName, String tenantDomain, String version,
            MessageContext messageContext) {
        List<String> apiAttributes = new ArrayList<>();
        String apiId = null;
        String label;
        try {
            apiId = gatewayArtifactsMgtDAO.getGatewayAPIId(apiName, version, tenantDomain);
            label = gatewayArtifactsMgtDAO.getGatewayAPILabel(apiId);
        } catch (APIManagementException e) {
            JSONObject responseObj = new JSONObject();
            responseObj.put("Message", "Error retrieving apiID and label of  " + apiName+ " from DB");
            String responseStringObj = String.valueOf(responseObj);
            log.error("Error retrieving apiID and label of  " + apiName+ " from DB", e);
            return Response.serverError().entity(responseStringObj).build();
        }
        apiAttributes.add(apiId);
        apiAttributes.add(label);
        synapseArtifactListDTOS.setApiId(apiId);
        synapseArtifactListDTOS.setLabel(label);
        return Response.ok().entity(synapseArtifactListDTOS).build();
    }
}
