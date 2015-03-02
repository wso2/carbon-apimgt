/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.synapse.endpoints;

import com.damnhandy.uri.template.impl.ExpressionParseException;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.mediators.MediatorProperty;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.util.xpath.SynapseXPath;
import com.damnhandy.uri.template.UriTemplate;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class HTTPEndpoint extends AbstractEndpoint {

    private UriTemplate uriTemplate;

    private String httpMethod;
    private SynapseXPath httpMethodExpression;

    /*Todo*/
    /*Do we need HTTP Headers here?*/


    public void onFault(MessageContext synCtx) {

        // is this really a fault or a timeout/connection close etc?
        if (isTimeout(synCtx)) {
            getContext().onTimeout();
        } else if (isSuspendFault(synCtx)) {
            getContext().onFault();
        }

        // this should be an ignored error if we get here
        setErrorOnMessage(synCtx, null, null);
        super.onFault(synCtx);
    }

    public void onSuccess() {
        if (getContext() != null) {
            getContext().onSuccess();
        }
    }

    public void send(MessageContext synCtx) {
        executeEpTypeSpecificFunctions(synCtx);
        if (getParentEndpoint() == null && !readyToSend()) {
            // if the this leaf endpoint is too a root endpoint and is in inactive
            informFailure(synCtx, SynapseConstants.ENDPOINT_ADDRESS_NONE_READY,
                    "Currently , Address endpoint : " + getContext());
        } else {
            super.send(synCtx);
        }
    }

    public void executeEpTypeSpecificFunctions(MessageContext synCtx) {
        processUrlTemplate(synCtx);
        processHttpMethod(synCtx);
    }

    private void processHttpMethod(MessageContext synCtx) {
        if (httpMethod != null) {
            super.getDefinition().setHTTPEndpoint(true);
            if (httpMethod.equalsIgnoreCase(Constants.Configuration.HTTP_METHOD_POST)) {
                synCtx.setProperty(Constants.Configuration.HTTP_METHOD,
                        Constants.Configuration.HTTP_METHOD_POST);
            } else if (httpMethod.equalsIgnoreCase(Constants.Configuration.HTTP_METHOD_GET)) {
                synCtx.setProperty(Constants.Configuration.HTTP_METHOD,
                        Constants.Configuration.HTTP_METHOD_GET);
            } else if(httpMethod.equalsIgnoreCase("patch")){
                synCtx.setProperty(Constants.Configuration.HTTP_METHOD, "PATCH");
            } else if (httpMethod.equalsIgnoreCase(Constants.Configuration.HTTP_METHOD_PUT)) {
                synCtx.setProperty(Constants.Configuration.HTTP_METHOD,
                        Constants.Configuration.HTTP_METHOD_PUT);
            } else if (httpMethod.equalsIgnoreCase(Constants.Configuration.HTTP_METHOD_DELETE)) {
                synCtx.setProperty(Constants.Configuration.HTTP_METHOD,
                        Constants.Configuration.HTTP_METHOD_DELETE);
            } else if (httpMethod.equalsIgnoreCase(Constants.Configuration.HTTP_METHOD_HEAD)) {
                synCtx.setProperty(Constants.Configuration.HTTP_METHOD,
                        Constants.Configuration.HTTP_METHOD_HEAD);
            }
        }
    }

    private void processUrlTemplate(MessageContext synCtx) throws ExpressionParseException {
        Map<String, Object> variables = new HashMap<String, Object>();

        /*The properties with uri.var.* are only considered for Outbound REST Endpoints*/
        Set propertySet = synCtx.getPropertyKeySet();

        for (Object propertyKey : propertySet) {
            if (propertyKey.toString() != null&&
                    (propertyKey.toString().startsWith(RESTConstants.REST_URI_VARIABLE_PREFIX)
                            || propertyKey.toString().startsWith(RESTConstants.REST_QUERY_PARAM_PREFIX))) {
                if (synCtx.getProperty(propertyKey.toString()) != null) {
                    variables.put(propertyKey.toString(), synCtx.getProperty(propertyKey.toString()));
                }
            }
        }

        // Include properties defined at endpoint.
        Iterator endpointProperties = getProperties().iterator();
        while(endpointProperties.hasNext()) {
            MediatorProperty property = (MediatorProperty) endpointProperties.next();
            if(property.getName().toString() != null
                    && (property.getName().toString().startsWith(RESTConstants.REST_URI_VARIABLE_PREFIX) ||
                    property.getName().toString().startsWith(RESTConstants.REST_QUERY_PARAM_PREFIX) )) {
                variables.put(property.getName(), property.getValue());
            }
        }

        // We have to create a local UriTemplate object, or else the UriTemplate.set(variables) call will fill up a list of variables and since uriTemplate
        // is not thread safe, this list won't be clearing.
        UriTemplate template = null;
        template = UriTemplate.fromTemplate(uriTemplate.getTemplate());

        if(template != null) {
            template.set(variables);
        }

        String evaluatedUri = "";
        if(variables.isEmpty()){
        	evaluatedUri = template.getTemplate();
        }else{
            try {
                URL url = new URL(URLDecoder.decode(template.expand(), "UTF-8"));
                evaluatedUri = url.toURI().toString();
                /*evaluatedUri = template.expand();
                evaluatedUri=evaluatedUri.replace("%3A", ":");
                evaluatedUri=evaluatedUri.replace("%2F", "/");*/
            } catch(URISyntaxException e) {
                log.debug("Invalid URL syntax for HTTP Endpoint: " + this.getName());
                URL url;
                try{
                    url = new URL(URLDecoder.decode(template.expand(), "UTF-8"));
                    evaluatedUri = url.toString();
                } catch (Exception e1){
                    log.debug("Error while decoding URL: " + this.getName());
                }

            } catch(MalformedURLException e) {
                log.debug("Invalid URL for HTTP Endpoint: " + this.getName());
                evaluatedUri = template.getTemplate();
            } catch(UnsupportedEncodingException e) {
                log.debug("Exception while decoding the URL in HTTP Endpoint: " + this.getName());
                evaluatedUri = template.getTemplate();
            } catch(ExpressionParseException e) {
                log.debug("No URI Template variables defined in HTTP Endpoint: " + this.getName());
                evaluatedUri = template.getTemplate();
            }
        }

        if (evaluatedUri != null) {
            synCtx.setTo(new EndpointReference(evaluatedUri));
            if (super.getDefinition() != null) {
                super.getDefinition().setAddress(evaluatedUri);
            }
        }
    }

    public String getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public UriTemplate getUriTemplate() {
        return uriTemplate;
    }

    public SynapseXPath getHttpMethodExpression() {
        return httpMethodExpression;
    }

    public void setUriTemplate(UriTemplate uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    public void setHttpMethodExpression(SynapseXPath httpMethodExpression) {
        this.httpMethodExpression = httpMethodExpression;
    }
}
