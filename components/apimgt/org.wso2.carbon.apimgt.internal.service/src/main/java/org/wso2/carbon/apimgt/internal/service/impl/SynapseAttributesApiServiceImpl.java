package org.wso2.carbon.apimgt.internal.service.impl;

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

    @Override public Response synapseAttributesGet(String apiName, String tenantDomain, String version,
            MessageContext messageContext) throws APIManagementException {
        List<String> apiAttributes = new ArrayList<>();
        String apiId = gatewayArtifactsMgtDAO.getGatewayAPIId(apiName, version, tenantDomain);
        String label = gatewayArtifactsMgtDAO.getGatewayAPILabel(apiId);
        apiAttributes.add(apiId);
        apiAttributes.add(label);
        synapseArtifactListDTOS.setApiId(apiId);
        synapseArtifactListDTOS.setLabel(label);
        return Response.ok().entity(synapseArtifactListDTOS).build();
    }
}
