/**
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

package org.apache.synapse.commons.json;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.InputStream;

public final class JsonBuilder implements Builder {
    private static final Log logger = LogFactory.getLog(JsonBuilder.class.getName());

    public OMElement processDocument(InputStream inputStream, String s,
                                     MessageContext messageContext) throws AxisFault {
        OMElement element = JsonUtil.toXml(inputStream, true);
        if (logger.isDebugEnabled()) {
            logger.debug("#processDocument. Built XML payload from JSON stream. MessageID: " + messageContext.getMessageID());
        }
        return element;
    }

    /**
     * @deprecated Use {@link org.apache.synapse.commons.json.JsonUtil#toXml(java.io.InputStream, boolean)}
     * @param jsonStream
     * @param pIs
     * @return OMElement
     */
    public static OMElement toXml(InputStream jsonStream, boolean pIs) throws AxisFault {
        return JsonUtil.toXml(jsonStream, pIs);
    }
}

