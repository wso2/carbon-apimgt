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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SequenceType;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.SequenceMediatorFactory;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.mediators.base.SequenceMediator;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.Resource;
import org.apache.synapse.rest.dispatch.URITemplateHelper;
import org.apache.synapse.rest.dispatch.URLMappingHelper;

import javax.xml.namespace.QName;
import java.util.Properties;

public class ResourceFactory {

    private static final Log log = LogFactory.getLog(ResourceFactory.class);

    public static Resource createResource(OMElement resourceElt) {
        return createResource(resourceElt, new Properties());
    }

    public static Resource createResource(OMElement resourceElt, Properties properties) {
        Resource resource = new Resource();
        configureURLMappings(resource, resourceElt);
        configureSequences(resource, resourceElt, properties);
        configureFilters(resource, resourceElt);
        return resource;
    }

    private static void configureFilters(Resource resource, OMElement resourceElt) {
        OMAttribute protocolAtt = resourceElt.getAttribute(new QName("protocol"));
        if (protocolAtt != null && !"".equals(protocolAtt.getAttributeValue())) {
            if (Constants.TRANSPORT_HTTP.equals(protocolAtt.getAttributeValue())) {
                resource.setProtocol(RESTConstants.PROTOCOL_HTTP_ONLY);
            } else if (Constants.TRANSPORT_HTTPS.equals(protocolAtt.getAttributeValue())) {
                resource.setProtocol(RESTConstants.PROTOCOL_HTTPS_ONLY);
            } else {
                handleException("Invalid protocol name: " + protocolAtt.getAttributeValue());
            }
        }

        OMAttribute contentTypeAtt = resourceElt.getAttribute(new QName("content-type"));
        if (contentTypeAtt != null && !"".equals(contentTypeAtt.getAttributeValue())) {
            resource.setContentType(contentTypeAtt.getAttributeValue());
        }

        OMAttribute userAgentAtt = resourceElt.getAttribute(new QName("user-agent"));
        if (userAgentAtt != null && !"".equals(userAgentAtt.getAttributeValue())) {
            resource.setUserAgent(userAgentAtt.getAttributeValue());
        }

        OMAttribute methodsAtt = resourceElt.getAttribute(new QName("methods"));
        if (methodsAtt != null && !"".equals(methodsAtt.getAttributeValue())) {
            String[] methods = methodsAtt.getAttributeValue().trim().split(" ");
            for (String method : methods) {
                boolean added = resource.addMethod(method);
                if (!added) {
                    handleException("Invalid or duplicate method definition for resource");
                }
            }
        }
    }

    private static void configureURLMappings(Resource resource, OMElement resourceElt) {
        OMAttribute urlMappingAtt = resourceElt.getAttribute(new QName("url-mapping"));
        OMAttribute uriTemplateAtt = resourceElt.getAttribute(new QName("uri-template"));
        if (urlMappingAtt != null && !"".equals(urlMappingAtt.getAttributeValue())) {
            resource.setDispatcherHelper(new URLMappingHelper(urlMappingAtt.getAttributeValue()));
        } else if (uriTemplateAtt != null && !"".equals(uriTemplateAtt.getAttributeValue())) {
            resource.setDispatcherHelper(new URITemplateHelper(uriTemplateAtt.getAttributeValue()));
        }
    }

    private static void configureSequences(Resource resource, OMElement resourceElt, Properties properties) {
        OMAttribute inSequenceKeyAtt = resourceElt.getAttribute(new QName("inSequence"));
        OMElement inSequenceElt = resourceElt.getFirstChildWithName(new QName(
                XMLConfigConstants.SYNAPSE_NAMESPACE, "inSequence"));
        if (inSequenceKeyAtt != null && !"".equals(inSequenceKeyAtt.getAttributeValue())) {
            resource.setInSequenceKey(inSequenceKeyAtt.getAttributeValue());
        } else if (inSequenceElt != null) {
            SequenceMediatorFactory fac = new SequenceMediatorFactory();
            SequenceMediator sequence = fac.createAnonymousSequence(inSequenceElt, properties);
            sequence.setSequenceType(SequenceType.API_INSEQ);
            resource.setInSequence(sequence);
        }

        OMAttribute outSequenceKeyAtt = resourceElt.getAttribute(new QName("outSequence"));
        OMElement outSequenceElt = resourceElt.getFirstChildWithName(new QName(
                XMLConfigConstants.SYNAPSE_NAMESPACE, "outSequence"));
        if (outSequenceKeyAtt != null && !"".equals(outSequenceKeyAtt.getAttributeValue())) {
            resource.setOutSequenceKey(outSequenceKeyAtt.getAttributeValue());
        } else if (outSequenceElt != null) {
            SequenceMediatorFactory fac = new SequenceMediatorFactory();
            SequenceMediator sequence = fac.createAnonymousSequence(outSequenceElt, new Properties());
            sequence.setSequenceType(SequenceType.API_OUTSEQ);
            resource.setOutSequence(sequence);
        }

        OMAttribute faultSequenceKeyAtt = resourceElt.getAttribute(new QName("faultSequence"));
        OMElement faultSequenceElt = resourceElt.getFirstChildWithName(new QName(
                XMLConfigConstants.SYNAPSE_NAMESPACE, "faultSequence"));
        if (faultSequenceKeyAtt != null && !"".equals(faultSequenceKeyAtt.getAttributeValue())) {
            resource.setFaultSequenceKey(faultSequenceKeyAtt.getAttributeValue());
        } else if (faultSequenceElt != null) {
            SequenceMediatorFactory fac = new SequenceMediatorFactory();
            SequenceMediator sequence = fac.createAnonymousSequence(faultSequenceElt, new Properties());
            sequence.setSequenceType(SequenceType.API_FAULTSEQ);
            resource.setFaultSequence(sequence);
        }
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }
}
