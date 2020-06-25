package org.wso2.carbon.apimgt.internal.service.impl;

import com.google.common.io.ByteStreams;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.internal.service.GatewaySynapseArtifactsApiService;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.internal.service.dto.SynapseArtifactListDTO;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;



public class GatewaySynapseArtifactsApiServiceImpl implements GatewaySynapseArtifactsApiService {

    protected ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

    private SynapseArtifactListDTO synapseArtifactListDTOS = new SynapseArtifactListDTO();

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

        synapseArtifactListDTOS.list(gatewayRuntimeArtifactsArray);
        synapseArtifactListDTOS.setCount(gatewayRuntimeArtifactsArray.size());
        return Response.ok().entity(synapseArtifactListDTOS).build();
    }
}
