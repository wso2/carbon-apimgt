/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.security;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpStatus;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.xml.rest.VersionStrategyFactory;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.*;
import org.apache.synapse.rest.dispatch.RESTDispatcher;
import org.wso2.carbon.apimgt.gateway.handlers.Utils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.RegistryType;
import org.wso2.carbon.registry.api.Registry;
import org.wso2.carbon.registry.api.RegistryException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.util.Arrays;
import java.util.Map;

/**
 * This handler functions as an enabler for the Store API Console
 * Sets the Access-Control-Allow-Origin header
 *
 */
public class CORSRequestHandler extends AbstractHandler implements ManagedLifecycle {

    private static final Log log = LogFactory.getLog(CORSRequestHandler.class);
    private String inline;
    private String cors;

    public void init(SynapseEnvironment synapseEnvironment) {
        log.debug("Initializing API authentication handler instance");
    }

    public void destroy() {
        log.debug("Destroying API authentication handler instance");
    }

    public boolean handleRequest(MessageContext messageContext) {
        String apiContext = (String) messageContext.getProperty(RESTConstants.REST_API_CONTEXT);
        String apiVersion = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API_VERSION);
        String httpMethod = (String) ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                getProperty(Constants.Configuration.HTTP_METHOD);
        API selectedApi = null;
        Resource selectedResource = null;
        boolean status;

        for (API api : messageContext.getConfiguration().getAPIs()) {
            if (apiContext.equals(api.getContext()) && apiVersion.equals(api.getVersion())) {
                selectedApi = api;
                break;
            }
        }
        String subPath;
        String path = RESTUtils.getFullRequestPath(messageContext);
        if (selectedApi.getVersionStrategy().getVersionType().equals(VersionStrategyFactory.TYPE_URL)) {
            //for URL based
            //request --> http://{host:port}/context/version/path/to/resource
            subPath = path.substring(
                    selectedApi.getContext().length() + selectedApi.getVersionStrategy().getVersion().length() + 1);
        } else {
            subPath = path.substring(selectedApi.getContext().length());
        }
        if ("".equals(subPath)) {
            subPath = "/";
        }
        messageContext.setProperty(RESTConstants.REST_SUB_REQUEST_PATH, subPath);

        if (selectedApi.getResources().length > 0) {
            for (RESTDispatcher dispatcher : RESTUtils.getDispatchers()) {
                Resource resource = dispatcher.findResource(messageContext, Arrays.asList(selectedApi.getResources()));
                if (resource != null && Arrays.asList(resource.getMethods()).contains(httpMethod)) {
                    selectedResource = resource;
                    break;
                }
            }
        }
        String resourceString = selectedResource != null ? selectedResource.getDispatcherHelper().getString() : null;
        String resourceCacheKey = APIUtil
                .getResourceInfoDTOCacheKey(apiContext, apiVersion, resourceString, httpMethod);
        messageContext.setProperty(APIConstants.API_ELECTED_RESOURCE, resourceString);
        messageContext.setProperty(APIConstants.API_RESOURCE_CACHE_KEY, resourceCacheKey);
        setCORSHeaders(messageContext);

        if (Arrays.asList(selectedResource != null ? selectedResource.getMethods() : new String[0])
                  .contains(httpMethod) && !"OPTIONS".equals(httpMethod)) {
            if ("inline".equals(inline)) {
                messageContext.getSequence("cors").mediate(messageContext);
            }
            return true;
        } else {
            messageContext.getSequence("cors").mediate(messageContext);
            Utils.sendFault(messageContext, HttpStatus.SC_OK);
            return false;
        }
    }

    public boolean handleResponse(MessageContext messageContext) {
        return true;
    }

    /**
     * @param messageContext message context for set cors headers as properties
     */
    public void setCORSHeaders(MessageContext messageContext) {
        org.apache.axis2.context.MessageContext axis2MC =
                ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        Map<String, String> headers =
                (Map) axis2MC.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        String requestOrigin = headers.get("Origin");
        if (cors != null && !cors.isEmpty()) {
            Registry
                    registryType =
                    CarbonContext.getThreadLocalCarbonContext().getRegistry(RegistryType.SYSTEM_GOVERNANCE);
            try {
                org.wso2.carbon.registry.api.Resource resource = registryType.get(cors.split("gov:")[1]);
                StAXOMBuilder builder = new StAXOMBuilder(resource.getContentStream());
                OMElement docElement = builder.getDocumentElement();
                messageContext.setProperty(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, docElement
                        .getFirstChildWithName(new QName(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_ORIGIN))
                        .getText());
                messageContext.setProperty(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_METHODS, docElement
                        .getFirstChildWithName(new QName(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_METHODS))
                        .getText());
                messageContext.setProperty(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_HEADERS, docElement
                        .getFirstChildWithName(new QName(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_HEADERS))
                        .getText());
            } catch (RegistryException e) {
                log.error("Requested Resource : " + cors.split("gov:")[1] + "Couldn't found", e);
            } catch (XMLStreamException e) {
                log.error("Couldn't Create XML from  : " + cors.split("gov:")[1], e);
            }
        } else {
            messageContext.setProperty(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_ORIGIN,
                                       Utils.getAllowedOrigin(requestOrigin));
            messageContext.setProperty(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_METHODS, Utils
                    .getAllowedMethods());
            messageContext.setProperty(APIConstants.CORSHeaders.ACCESS_CONTROL_ALLOW_HEADERS, Utils
                    .getAllowedHeaders());
        }

    }

    public String getInline() {
        return inline;
    }

    public void setInline(String inline) {
        this.inline = inline;
    }

    public String getCors() {
        return cors;
    }

    public void setCors(String cors) {
        this.cors = cors;
    }
}
