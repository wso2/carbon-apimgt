/*
 * Copyright 2005,2006 WSO2, Inc. http://www.wso2.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.synapse.transport.passthru.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axiom.soap.SOAPHeaderBlock;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AbstractDispatcher;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.axis2.util.Utils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.transport.passthru.PassThroughConstants;
import org.apache.synapse.transport.passthru.Pipe;

import java.util.Map;

public class RelaySecuirtyMessageBuilderDispatchandler  extends AbstractDispatcher{

	private static final Log log = LogFactory.getLog(RelaySecuirtyMessageBuilderDispatchandler.class);
	
	private static final String APPLICATION_XML = "application/xml";
	private static final String WSSE = "wsse";
	private static final String WSS_WSSECURITY_SECEXT_1_0_XSD = "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd";
	private static final String PROXY = "proxy";
	private static final String SERVICE_TYPE = "serviceType";
	public static final String NAME = "RelaySecuirtyMessageBuilderDispatchandler";
	
	
	@Override
	public InvocationResponse invoke(MessageContext messageContext) throws AxisFault {
		InvocationResponse invocationResponse = super.invoke(messageContext);

		EndpointReference toEPR = messageContext.getTo();

		Pipe pipe = (Pipe) messageContext.getProperty(PassThroughConstants.PASS_THROUGH_PIPE);
		
		if (pipe != null) {
			if (toEPR != null) {

				ConfigurationContext configurationContext = messageContext.getConfigurationContext();
				AxisConfiguration registry = configurationContext.getAxisConfiguration();
				String filePart = toEPR.getAddress();

				if (filePart != null) {
					String serviceOpPart = Utils.getServiceAndOperationPart(filePart,
					                       messageContext.getConfigurationContext().getServiceContextPath());

					AxisService axisService = null;

					// only service context path onwards values will be taken
					if (messageContext.getConfigurationContext().getServiceContextPath() != null &&
					    serviceOpPart != null) {
						axisService = registry.getService(serviceOpPart);
						if (axisService != null) {
							Parameter parameter = axisService.getParameter(SERVICE_TYPE);
							if (parameter != null) {
								if (!parameter.getValue().equals(PROXY)) {
									build(messageContext);
								}
							} else {
								build(messageContext);
							}
						}
					}
				}

			}
			if (messageContext.isEngaged(PassThroughConstants.SECURITY_MODULE_NAME)) {
				SOAPHeader header = null;
				if (messageContext.getEnvelope().getHeader() != null) {
					header = messageContext.getEnvelope().getHeader();
				}
				build(messageContext);
				this.handlePOXRequests(messageContext, header);
			}
		}

		return invocationResponse;
	}

	private void handlePOXRequests(MessageContext messageContext, SOAPHeader header) {
	    String contentType = (String) messageContext.getProperty(Constants.Configuration.CONTENT_TYPE);
	    String _contentType =contentType;
	    if (contentType != null) {
	    	int j = contentType.indexOf(";");
	    	if (j > 0) {
	    		_contentType = contentType.substring(0, j);
	    	}
	    }
	     
	    //if the request message is a POX and if authenticate enables, which means a custom security header added to the SOAP header
	    //and in PT case, since the message is getting build forcefully we need to make sure the POX security headers added by POXSecurityHandler
	    //is existing in the newly build soap envelope.

        /*Handling SOAP with BasicAuth*/
        boolean isSOAPWithBasicAuth = false;
        Object o = messageContext.getProperty(MessageContext.TRANSPORT_HEADERS);
        if (o != null && o instanceof Map) {
            Map httpHeaders = (Map) o;
            for (Object httpHeader : httpHeaders.keySet()) {
                Object value = httpHeaders.get(httpHeader);
                if (httpHeader instanceof String && value != null && value instanceof String) {
                    if (HTTPConstants.HEADER_AUTHORIZATION.equalsIgnoreCase((String) httpHeader) && ((String) value).startsWith("Basic")) {
                        isSOAPWithBasicAuth = true;
                        break;
                    }
                }
            }
        }

        if (_contentType != null && _contentType.equals(APPLICATION_XML) && header != null
                && header.getChildElements() != null
                || messageContext.isDoingREST()
                || isSOAPWithBasicAuth) {
            try {
                OMElement element = AXIOMUtil.stringToOM(header.toString());
                OMNamespace omNamespace =
                        OMAbstractFactory.getOMFactory().createOMNamespace(WSS_WSSECURITY_SECEXT_1_0_XSD, WSSE);
                SOAPHeaderBlock soapBloackingHeader = OMAbstractFactory.getSOAP12Factory().createSOAPHeaderBlock("Security", omNamespace);
                OMElement securityHeader = element.getFirstElement();
                if (securityHeader != null) {
                    while (securityHeader.getChildElements().hasNext()) {
                        soapBloackingHeader.addChild((OMNode) securityHeader.getChildElements().next());
                    }

                    messageContext.getEnvelope().getHeader().addChild(soapBloackingHeader);
                }
            } catch (Exception e) {
                log.error("Error while executing the message at relaySecurity handler", e);
            }

        }
    }

	private void build(MessageContext messageContext) {
	    try {
	    	RelayUtils.buildMessage(messageContext, false);
	    } catch (Exception e) {
	    	 log.error("Error while executing the message at relaySecurity handler", e);
	    }
    }

	@Override
	public AxisOperation findOperation(AxisService arg0, MessageContext arg1)
			throws AxisFault {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public AxisService findService(MessageContext arg0) throws AxisFault {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initDispatcher() {
		init(new HandlerDescription(NAME));
	}
	
}
