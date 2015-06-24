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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.config.xml.SequenceMediatorSerializer;
import org.apache.synapse.rest.RESTConstants;
import org.apache.synapse.rest.Resource;
import org.apache.synapse.rest.dispatch.DispatcherHelper;
import org.apache.synapse.rest.dispatch.URITemplateHelper;
import org.apache.synapse.rest.dispatch.URLMappingHelper;

public class ResourceSerializer {

    private static final OMFactory fac = OMAbstractFactory.getOMFactory();

    public static OMElement serializeResource(Resource resource) {
        OMElement resourceElt = fac.createOMElement("resource", SynapseConstants.SYNAPSE_OMNAMESPACE);
        String[] methods = resource.getMethods();
        if (methods.length > 0) {
            String value = "";
            for (String method : methods) {
                value += method + " ";
            }
            resourceElt.addAttribute("methods", value.trim(), null);
        }

        if (resource.getContentType() != null) {
            resourceElt.addAttribute("content-type", resource.getContentType(), null);
        }
        if (resource.getUserAgent() != null) {
            resourceElt.addAttribute("user-agent", resource.getUserAgent(), null);
        }
        if (resource.getProtocol() == RESTConstants.PROTOCOL_HTTP_ONLY) {
            resourceElt.addAttribute("protocol", "http", null);
        } else if (resource.getProtocol() == RESTConstants.PROTOCOL_HTTPS_ONLY) {
            resourceElt.addAttribute("protocol", "https", null);
        }

        DispatcherHelper helper = resource.getDispatcherHelper();
        if (helper != null) {
            if (helper instanceof URLMappingHelper) {
                resourceElt.addAttribute("url-mapping", helper.getString(), null);
            } else if (helper instanceof URITemplateHelper) {
                resourceElt.addAttribute("uri-template", helper.getString(), null);
            }
        }

        SequenceMediatorSerializer seqSerializer = new SequenceMediatorSerializer();
        if (resource.getInSequenceKey() != null) {
            resourceElt.addAttribute("inSequence", resource.getInSequenceKey(), null);
        } else if (resource.getInSequence() != null) {
            OMElement inSeqElement = seqSerializer.serializeAnonymousSequence(
                    null, resource.getInSequence());
            inSeqElement.setLocalName("inSequence");
            resourceElt.addChild(inSeqElement);
        }

        if (resource.getOutSequenceKey() != null) {
            resourceElt.addAttribute("outSequence", resource.getOutSequenceKey(), null);
        } else if (resource.getOutSequence() != null) {
            OMElement outSeqElement = seqSerializer.serializeAnonymousSequence(
                    null, resource.getOutSequence());
            outSeqElement.setLocalName("outSequence");
            resourceElt.addChild(outSeqElement);
        }

        if (resource.getFaultSequenceKey() != null) {
            resourceElt.addAttribute("faultSequence", resource.getFaultSequenceKey(), null);
        } else if (resource.getFaultSequence() != null) {
            OMElement faultSeqElement = seqSerializer.serializeAnonymousSequence(
                    null, resource.getFaultSequence());
            faultSeqElement.setLocalName("faultSequence");
            resourceElt.addChild(faultSeqElement);
        }

        return resourceElt;
    }

}
