package org.wso2.carbon.apimgt.gateway.opa;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.axis2.Constants;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.ProtocolVersion;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.opa.OPARequestGenerator;
import org.apache.synapse.transport.passthru.ServerWorker;
import org.apache.synapse.transport.passthru.SourceRequest;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;

import java.util.Map;
import java.util.TreeMap;

public class APIMOPARequestGenerator implements OPARequestGenerator{
    private static final Log log = LogFactory.getLog(APIMOPARequestGenerator.class);

    public static final String HTTP_METHOD_STRING = "HTTP_METHOD";
    public static final String API_BASEPATH_STRING = "TransportInURL";

    @Override
    public String createRequest(MessageContext messageContext, Map<String, Object> advancedProperties) {

        JSONObject opaPayload = new JSONObject();

        org.apache.axis2.context.MessageContext axis2MessageContext = ((Axis2MessageContext) messageContext)
                .getAxis2MessageContext();
        TreeMap<String, String> transportHeadersMap = (TreeMap<String, String>) axis2MessageContext
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        String requestOriginIP = getIp(axis2MessageContext);
        String requestMethod = (String) axis2MessageContext.getProperty(HTTP_METHOD_STRING);
        String requestPath = (String) axis2MessageContext.getProperty(API_BASEPATH_STRING);
        String requestHttpVersion = getHttpVersion(axis2MessageContext);

        AuthenticationContext authContext = (AuthenticationContext) messageContext.getProperty("__API_AUTH_CONTEXT");
        if (authContext != null) {
            String authContextSting = new Gson().toJson(authContext);
            JSONParser parser = new JSONParser();
            JSONObject authContextJson;
            try {
                authContextJson = (JSONObject) parser.parse(authContextSting);
                //User Info is added only if the request is Authenticated
                opaPayload.put("AuthContext", authContextJson);
            } catch (ParseException e) {
                log.error("Error occurred when parsing authContext String", e);
                opaPayload.put("AuthContext", authContextSting);
            }
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String transportHeadersJson = gson.toJson(transportHeadersMap);

        opaPayload.put("requestOrigin", requestOriginIP);
        opaPayload.put("method", requestMethod);
        opaPayload.put("path", requestPath);
        opaPayload.put("httpVersion", requestHttpVersion);
        opaPayload.put("transportHeaders", transportHeadersJson);
        return opaPayload.toJSONString();
    }

    @Override
    public boolean handleResponse(MessageContext messageContext, String responseString) {
        boolean serverResponse = true;
        if (responseString.equals("{}")) {
            //The policy for this API has not been created at the OPA server. Request will be sent to
            // backend without validation
            if (log.isDebugEnabled()) {
                log.debug("OPA Policy was not defined for the API ");
            }
        } else {
            JSONParser parser = new JSONParser();
            try {
                JSONObject responseObject = (JSONObject) parser.parse(responseString);
                Object resultObject = responseObject.get("result");
                if (resultObject != null) {
                    serverResponse = JavaUtils.isTrueExplicitly(resultObject);
                }
            } catch (ParseException e) {
                log.error("Parsing exception for response");
            }
        }
        return serverResponse;
    }

    public String getIp(org.apache.axis2.context.MessageContext axis2MessageContext) {

        //Set transport headers of the message
        TreeMap<String, String> transportHeaderMap = (TreeMap<String, String>) axis2MessageContext
                .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        // Assigning an Empty String so that when doing comparisons, .equals method can be used without explicitly
        // checking for nullity.
        String remoteIP = "";
        //Check whether headers map is null and x forwarded for header is present
        if (transportHeaderMap != null) {
            remoteIP = transportHeaderMap.get("X-Forwarded-For");
        }

        //Setting IP of the client by looking at x forded for header and  if it's empty get remote address
        if (remoteIP != null && !remoteIP.isEmpty()) {
            if (remoteIP.indexOf(",") > 0) {
                remoteIP = remoteIP.substring(0, remoteIP.indexOf(","));
            }
        } else {
            remoteIP = (String) axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
        }
        if (remoteIP.indexOf(":") > 0) {
            remoteIP = remoteIP.substring(0, remoteIP.indexOf(":"));
        }
        return remoteIP;
    }

    public String getHttpVersion(org.apache.axis2.context.MessageContext axis2MessageContext) {

        ServerWorker worker = (ServerWorker) axis2MessageContext.getProperty(Constants.OUT_TRANSPORT_INFO);
        SourceRequest sourceRequest = worker.getSourceRequest();
        ProtocolVersion httpProtocolVersion = sourceRequest.getVersion();

        return httpProtocolVersion.getMajor() + "." + httpProtocolVersion
                .getMinor();
    }
}
