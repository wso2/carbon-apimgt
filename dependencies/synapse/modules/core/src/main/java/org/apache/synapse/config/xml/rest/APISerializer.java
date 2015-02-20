/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.apache.synapse.config.xml.rest;

import org.apache.axiom.om.*;
import org.apache.axis2.Constants;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.Handler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.Resource;

import java.util.Iterator;
import java.util.Map;

public class APISerializer {

    private static final OMFactory fac = OMAbstractFactory.getOMFactory();

    protected static final OMNamespace nullNS
            = fac.createOMNamespace(XMLConfigConstants.NULL_NAMESPACE, "");

    public static OMElement serializeAPI(API api) {
        OMElement apiElt = fac.createOMElement("api", SynapseConstants.SYNAPSE_OMNAMESPACE);
        apiElt.addAttribute("name", api.getAPIName(), null);
        apiElt.addAttribute("context", api.getContext(), null);

        VersionStrategySerializer.serializeVersioningStrategy(api.getVersionStrategy(), apiElt) ;
        if (api.getHost() != null) {
            apiElt.addAttribute("hostname", api.getHost(), null);
        }
        if (api.getPort() != -1) {
            apiElt.addAttribute("port", String.valueOf(api.getPort()), null);
        }

        Resource[] resources = api.getResources();
        for (Resource r : resources) {
            OMElement resourceElt = ResourceSerializer.serializeResource(r);
            apiElt.addChild(resourceElt);
        }

        Handler[] handlers = api.getHandlers();
        if (handlers.length > 0) {
            OMElement handlersElt = fac.createOMElement("handlers", SynapseConstants.SYNAPSE_OMNAMESPACE);
            for (Handler handler : handlers) {
                OMElement handlerElt = fac.createOMElement("handler", SynapseConstants.SYNAPSE_OMNAMESPACE);
                handlerElt.addAttribute("class", handler.getClass().getName(), null);
                handlersElt.addChild(handlerElt);
                if (handler.getProperties()!=null) {
                    Map propertyMap = handler.getProperties();
                    if (propertyMap != null) {
                        Iterator itr = propertyMap.keySet().iterator();
                        while (itr.hasNext()) {
                            String propName = (String) itr.next();
                            Object o = handler.getProperties().get(propName);
                            OMElement prop = fac.createOMElement(APIFactory.PROP_Q, handlerElt);
                            prop.addAttribute(fac.createOMAttribute(APIFactory.ATT_NAME.getLocalPart(),
                                    nullNS, propName));
                            if (o instanceof String) {
                                prop.addAttribute(fac.createOMAttribute(APIFactory.ATT_VALUE.getLocalPart(),
                                        nullNS, (String) o));
                            } else {
                                prop.addChild((OMNode) o);
                            }
                            handlerElt.addChild(prop);
                        }
                    }
                }
            }
            apiElt.addChild(handlersElt);
        }

        if (api.getProtocol() == RESTConstants.PROTOCOL_HTTP_ONLY) {
            apiElt.addAttribute("transports", Constants.TRANSPORT_HTTP, null);
        } else if (api.getProtocol() == RESTConstants.PROTOCOL_HTTPS_ONLY) {
            apiElt.addAttribute("transports", Constants.TRANSPORT_HTTPS, null);
        }

        return apiElt;
    }

    public static OMElement serializeAPI(OMElement parent, API api) {
        OMElement apiElt = serializeAPI(api);
        if (parent != null) {
            parent.addChild(apiElt);
        }
        return apiElt;
    }

}
