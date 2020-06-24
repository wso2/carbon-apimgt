package org.wso2.carbon.apimgt.internal.service.impl;

import com.google.common.io.ByteStreams;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dao.constants.SQLConstants;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.internal.service.*;
import org.wso2.carbon.apimgt.internal.service.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.internal.service.dto.ErrorDTO;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import static org.h2.engine.Constants.UTF8;


public class SynapseArtifactsApiServiceImpl implements SynapseArtifactsApiService {

    protected ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

    @Override
    public Response synapseArtifactsGet( String apiId,String gatewayLabel, String gatewayInstruction,
                                        MessageContext messageContext) throws APIManagementException {
        String gatewayRuntimeArtifacts;
        try {
            ByteArrayInputStream byteStream =
                    apiMgtDAO.getGatewayPublishedAPIArtifacts(apiId, gatewayLabel, gatewayInstruction);
            byte[] bytes = ByteStreams.toByteArray(byteStream);
            gatewayRuntimeArtifacts = new String(bytes);
        } catch (IOException e ) {
            throw new APIManagementException("Error retrieving Artifact belongs to  " + apiId + " from DB", e);
        }
        return Response.ok().entity(gatewayRuntimeArtifacts).build();
    }

    @Override
    public Response synapseArtifactsPost(SynapseArtifactDTO body, MessageContext messageContext)
            throws APIManagementException {


        String bytesEncodedAsString = body.getBytesEncodedAsString();
        String apiId = body.getApiId();
        String apiName = body.getApiName();
        String gatewayInstruction = body.getGatewayInstruction();
        String gatewayLabel= body.getGatewayLabel();
        String tenantDomain = body.getTenantDomain();
        String version = body.getVersion();

        try {
            boolean status =false;
            byte[] gatewayRuntimeArtifactsAsBytes = bytesEncodedAsString.getBytes();
            byte[] bytesDecoded =  Base64.decodeBase64(gatewayRuntimeArtifactsAsBytes);
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytesDecoded);
            if (!apiMgtDAO.isAPIDetailsExists(apiId)) {
                apiMgtDAO.addGatewayPublishedAPIDetails(apiId, apiName,
                        version, tenantDomain);
            }
            String dbQuery;
            String decodedGatewaylabel = URLDecoder.decode(gatewayLabel,
                    APIConstants.DigestAuthConstants.CHARSET);
            if (apiMgtDAO.isAPIArtifactExists(apiId, decodedGatewaylabel)) {
                dbQuery = SQLConstants.UPDATE_API_ARTIFACT;
            } else {
                dbQuery = SQLConstants.ADD_GW_API_ARTIFACT;
            }
            apiMgtDAO.addGatewayPublishedAPIArtifacts(apiId, decodedGatewaylabel,
                    byteArrayInputStream, bytesDecoded.length, gatewayInstruction, dbQuery);
            status = true;
            JSONObject responseObj = new JSONObject();
            if (status) {
                responseObj.put("Message", "Success");
                String responseStringObj = String.valueOf(responseObj);
                return Response.ok().entity(responseStringObj).build();
            } else {
                responseObj.put("Message", "Error");
                String responseStringObj = String.valueOf(responseObj);
                return Response.serverError().entity(responseStringObj).build();
            }
        } catch (APIManagementException | UnsupportedEncodingException e) {
            throw new APIManagementException("Error saving Artifacts to the DB", e);
        }
    }
}
