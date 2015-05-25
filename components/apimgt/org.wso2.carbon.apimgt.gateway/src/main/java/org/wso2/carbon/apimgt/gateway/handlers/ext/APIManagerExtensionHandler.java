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

package org.wso2.carbon.apimgt.gateway.handlers.ext;

import org.apache.axis2.AxisFault;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

/**
 * A simple extension handler for the APIs deployed in the API gateway. This handler first
 * looks for a sequence named WSO2AM--Ext--[Dir], where [Dir] could be either In or Out
 * depending on the direction of the message. If such a sequence is found, it is invoked.
 * Following that a more API specific extension sequence is looked up by using the name
 * pattern provider--api--version--[Dir]. If such an API specific sequence is found, that
 * is also invoked. If no extension is found either at the global level or at the per API level
 * this mediator simply returns true.
 */
public class APIManagerExtensionHandler extends AbstractHandler {

    private static final String EXT_SEQUENCE_PREFIX = "WSO2AM--Ext--";
    private static final String DIRECTION_IN = "In";
    private static final String DIRECTION_OUT = "Out";
    private static final Log log = LogFactory.getLog(APIManagerExtensionHandler.class);

    public boolean mediate(MessageContext messageContext, String direction) {
        // In order to avoid a remote registry call occurring on each invocation, we
        // directly get the extension sequences from the local registry.
        Map localRegistry = messageContext.getConfiguration().getLocalRegistry();

        Object sequence = localRegistry.get(EXT_SEQUENCE_PREFIX + direction);
        if (sequence != null && sequence instanceof Mediator) {
            if (!((Mediator) sequence).mediate(messageContext)) {
                return false;
            }
        }

        String apiName = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
        sequence = localRegistry.get(apiName + "--" + direction);
        if (sequence != null && sequence instanceof Mediator) {
            return ((Mediator) sequence).mediate(messageContext);
        }
        return true;
    }

    public boolean handleRequest(MessageContext messageContext) {
        return mediate(messageContext, DIRECTION_IN);
    }

    public boolean handleResponse(MessageContext messageContext) {
        return mediate(messageContext, DIRECTION_OUT);
    }
}

