/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.usage.publisher;

import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.usage.publisher.dto.ResponsePublisherDTO;
import org.wso2.carbon.apimgt.usage.publisher.internal.UsageComponent;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;

/*
* This mediator is to publish events upon success API invocations
*/

public class APIMgtResponseHandler extends APIMgtCommonExecutionPublisher {

    public APIMgtResponseHandler() {
        super();
    }

    public boolean mediate(MessageContext mc) {
        super.mediate(mc);
        if (publisher == null) {
            this.initializeDataPublisher();
        }

        try {
            if (!enabled || skipEventReceiverConnection) {
                return true;
            }
            long responseSize = 0;
            long responseTime = 0;
            long serviceTime = 0;
            long backendTime = 0;
            long endTime = System.currentTimeMillis();
            boolean cacheHit = false;

            Object startTimeProperty = mc.getProperty(APIMgtGatewayConstants.REQUEST_START_TIME);
            long startTime = (startTimeProperty == null ? 0 :  Long.parseLong((String) startTimeProperty));

            Object beStartTimeProperty = mc.getProperty(APIMgtGatewayConstants.BACKEND_REQUEST_START_TIME);
            long backendStartTime = (beStartTimeProperty == null ? 0 : Long.parseLong((String) beStartTimeProperty));

            Object beEndTimeProperty = mc.getProperty(APIMgtGatewayConstants.BACKEND_REQUEST_END_TIME);
            long backendEndTime = (beEndTimeProperty == null ? 0 : ((Number) beEndTimeProperty).longValue());

            //Check the config property is set to true to build the response message in-order
            //to get the response message size
            boolean isBuildMsg = UsageComponent.getAmConfigService().getAPIAnalyticsConfiguration()
                    .isBuildMsg();
            org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) mc).
                    getAxis2MessageContext();
            if (isBuildMsg) {
                Map headers = (Map) axis2MC.getProperty(
                        org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
                String contentLength = (String) headers.get(HttpHeaders.CONTENT_LENGTH);
                if (contentLength != null) {
                    responseSize = Integer.parseInt(contentLength);
                } else {  //When chunking is enabled
                    try {
                        RelayUtils.buildMessage(axis2MC);
                    } catch (IOException ex) {
                        //In case of an exception, it won't be propagated up,and set response size to 0
                        log.error("Error occurred while building the message to" +
                                  " calculate the response body size", ex);
                    } catch (XMLStreamException ex) {
                        log.error("Error occurred while building the message to calculate the response" +
                                  " body size", ex);
                    }
                    
                    SOAPEnvelope env = mc.getEnvelope();
                    if (env != null) {
                        SOAPBody soapbody = env.getBody();
                        if (soapbody != null) {
                            byte[] size = soapbody.toString().getBytes(Charset.defaultCharset());
                            responseSize = size.length;
                        }
                    }
                    
                }
            }
            //When start time not properly set
            if (startTime == 0) {
                responseTime = 0;
                backendTime = 0;
                serviceTime = 0;
            } else if (endTime != 0 && backendStartTime != 0 && backendEndTime != 0) { //When
                // response caching is disabled
                responseTime = endTime - startTime;
                backendTime = backendEndTime - backendStartTime;
                serviceTime = responseTime - backendTime;

            } else if (endTime != 0 && backendStartTime == 0) {//When response caching enabled
                responseTime = endTime - startTime;
                serviceTime = responseTime;
                backendTime = 0;
                cacheHit = true;
            }

            ResponsePublisherDTO responsePublisherDTO = new ResponsePublisherDTO();
            responsePublisherDTO.setConsumerKey((String) mc.getProperty(APIMgtGatewayConstants.CONSUMER_KEY));
            responsePublisherDTO.setUsername((String) mc.getProperty(APIMgtGatewayConstants.USER_ID));
            String fullRequestPath = (String) mc.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
            String tenantDomain = MultitenantUtils.getTenantDomainFromRequestURL(fullRequestPath);
            responsePublisherDTO.setContext((String) mc.getProperty(APIMgtGatewayConstants.CONTEXT));
            String apiVersion = (String) mc.getProperty(RESTConstants.SYNAPSE_REST_API);
            responsePublisherDTO.setApiVersion(apiVersion);
            responsePublisherDTO.setApi((String) mc.getProperty(APIMgtGatewayConstants.API));
            responsePublisherDTO.setVersion((String) mc.getProperty(APIMgtGatewayConstants.VERSION));
            responsePublisherDTO.setResourcePath((String) mc.getProperty(APIMgtGatewayConstants.RESOURCE));
            responsePublisherDTO.setResourceTemplate((String) mc.getProperty(APIConstants.API_ELECTED_RESOURCE));
            responsePublisherDTO.setMethod((String) mc.getProperty(APIMgtGatewayConstants.HTTP_METHOD));
            responsePublisherDTO.setResponseTime(responseTime);
            responsePublisherDTO.setServiceTime(serviceTime);
            responsePublisherDTO.setBackendTime(backendTime);
            responsePublisherDTO.setHostName((String) mc.getProperty(APIMgtGatewayConstants.HOST_NAME));
            String apiPublisher = (String) mc.getProperty(APIMgtGatewayConstants.API_PUBLISHER);
            if (apiPublisher == null) {
                apiPublisher = APIUtil.getAPIProviderFromRESTAPI(apiVersion, tenantDomain);
            }
            responsePublisherDTO.setApiPublisher(apiPublisher);
            responsePublisherDTO.setTenantDomain(MultitenantUtils.getTenantDomain(apiPublisher));
            responsePublisherDTO.setApplicationName((String) mc.getProperty(APIMgtGatewayConstants.APPLICATION_NAME));
            responsePublisherDTO.setApplicationId((String) mc.getProperty(APIMgtGatewayConstants.APPLICATION_ID));
            responsePublisherDTO.setCacheHit(cacheHit);
            responsePublisherDTO.setResponseSize(responseSize);
            responsePublisherDTO.setEventTime(endTime);//This is the timestamp response event published
            responsePublisherDTO
                    .setDestination((String) mc.getProperty(APIMgtGatewayConstants.SYNAPSE_ENDPOINT_ADDRESS));
            responsePublisherDTO.setResponseCode((Integer) axis2MC.getProperty(SynapseConstants.HTTP_SC));

            String url = (String) mc.getProperty(RESTConstants.REST_URL_PREFIX);

            URL apiurl = new URL(url);
            int port = apiurl.getPort();
            String protocol = mc.getProperty(
                    SynapseConstants.TRANSPORT_IN_NAME) + "-" + port;
            responsePublisherDTO.setProtocol(protocol);
            publisher.publishEvent(responsePublisherDTO);

        } catch (Exception e) {
            log.error("Cannot publish response event. " + e.getMessage(), e);
        }
        return true; // Should never stop the message flow
    }

    public boolean isContentAware() {
        return false;
    }
}

