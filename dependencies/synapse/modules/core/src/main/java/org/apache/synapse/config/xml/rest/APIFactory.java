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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axis2.Constants;
import org.apache.axiom.om.OMNode;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.util.PropertyHelper;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.rest.API;
import org.apache.synapse.rest.Handler;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.version.VersionStrategy;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Properties;

public class APIFactory {

    private static final Log log = LogFactory.getLog(APIFactory.class);

    static final QName PROP_Q
        = new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "property");

    static final QName ATT_NAME = new QName("name");

    static final QName ATT_VALUE   = new QName("value");

    public static API createAPI(OMElement apiElt) {
        return createAPI(apiElt, new Properties());
    }

    public static API createAPI(OMElement apiElt, Properties properties) {
        OMAttribute nameAtt = apiElt.getAttribute(new QName("name"));
        if (nameAtt == null || "".equals(nameAtt.getAttributeValue())) {
            handleException("Attribute 'name' is required for an API definition");
        }

        OMAttribute contextAtt = apiElt.getAttribute(new QName("context"));
        if (contextAtt == null || "".equals(contextAtt.getAttributeValue())) {
            handleException("Attribute 'context' is required for an API definition");
        }

        API api = new API(nameAtt.getAttributeValue(), contextAtt.getAttributeValue());

        OMAttribute hostAtt = apiElt.getAttribute(new QName("hostname"));
        if (hostAtt != null && !"".equals(hostAtt.getAttributeValue())) {
            api.setHost(hostAtt.getAttributeValue());
        }

        VersionStrategy vStrategy = VersionStrategyFactory.createVersioningStrategy(api, apiElt);

        api.setVersionStrategy(vStrategy);

        OMAttribute portAtt = apiElt.getAttribute(new QName("port"));
        if (portAtt != null && !"".equals(portAtt.getAttributeValue())) {
            api.setPort(Integer.parseInt(portAtt.getAttributeValue()));
        }

        Iterator resources = apiElt.getChildrenWithName(new QName(
                XMLConfigConstants.SYNAPSE_NAMESPACE, "resource"));
        boolean noResources = true;
        while (resources.hasNext()) {
            OMElement resourceElt = (OMElement) resources.next();
            api.addResource(ResourceFactory.createResource(resourceElt, properties));
            noResources = false;
        }

        if (noResources) {
            handleException("An API must contain at least one resource definition");
        }

        OMElement handlersElt = apiElt.getFirstChildWithName(new QName(
                XMLConfigConstants.SYNAPSE_NAMESPACE, "handlers"));
        if (handlersElt != null) {
            Iterator handlers = handlersElt.getChildrenWithName(new QName(
                    XMLConfigConstants.SYNAPSE_NAMESPACE, "handler"));
            while (handlers.hasNext()) {
                OMElement handlerElt = (OMElement) handlers.next();
                defineHandler(api, handlerElt);
            }
        }

        OMAttribute trans = apiElt.getAttribute(
                new QName(XMLConfigConstants.NULL_NAMESPACE, "transports"));
        if (trans != null) {
            String transports = trans.getAttributeValue();
            if (!"".equals(transports)) {
                if (Constants.TRANSPORT_HTTP.equals(transports)) {
                    api.setProtocol(RESTConstants.PROTOCOL_HTTP_ONLY);
                } else if (Constants.TRANSPORT_HTTPS.equals(transports)) {
                    api.setProtocol(RESTConstants.PROTOCOL_HTTPS_ONLY);
                } else {
                    handleException("Invalid protocol name: " + transports);
                }
            }
        }
        return api;
    }

    private static void defineHandler(API api, OMElement handlerElt) {
        String handlerClass = handlerElt.getAttributeValue(new QName("class"));
        if (handlerClass == null || "".equals(handlerClass)) {
            handleException("A handler element must have a class attribute");
        }

        try {
            Class clazz = APIFactory.class.getClassLoader().loadClass(handlerClass);
            Handler handler = (Handler) clazz.newInstance();
            api.addHandler(handler);

            for (Iterator it = handlerElt.getChildrenWithName(PROP_Q); it.hasNext();) {
                OMElement child = (OMElement) it.next();

                String propName = child.getAttribute(ATT_NAME).getAttributeValue();
                if (propName == null) {
                    handleException(
                        "A Class mediator property must specify the name attribute");
                } else {
                    if (child.getAttribute(ATT_VALUE) != null) {
                        String value = child.getAttribute(ATT_VALUE).getAttributeValue();
                        handler.addProperty(propName, value);
                        PropertyHelper.setInstanceProperty(propName, value, handler);
                    } else {
                        OMNode omElt = child.getFirstElement();
                        if (omElt != null) {
                            handler.addProperty(propName, omElt);
                            PropertyHelper.setInstanceProperty(propName, omElt, handler);
                        } else {
                            handleException("A Class mediator property must specify " +
                                "name and value attributes, or a name and a child XML fragment");
                        }
                    }
                }
            }

        } catch (Exception e) {
            handleException("Error initializing API handler: " + handlerClass, e);
        }
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    private static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }
}
