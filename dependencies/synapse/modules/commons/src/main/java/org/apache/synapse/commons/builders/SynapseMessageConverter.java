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

package org.apache.synapse.commons.builders;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.OutputStream;

public abstract class SynapseMessageConverter implements MessageConverter {
    private static final Log logger = LogFactory.getLog(SynapseMessageConverter.class.getName());

    public void convert(OMElement element, OutputStream outputStream, String fromMediaType,
                        String toMediaType) {
        logger.warn("Must be overridden.");
    }

    public void convert(MessageContext messageContext, OutputStream outputStream,
                        String fromMediaType, String toMediaType) {
        logger.warn("Must be overridden.");
    }

    public StringBuilder convert(OMElement element, String fromMediaType, String toMediaType) {
        logger.warn("Must be overridden.");
        return new StringBuilder("");
    }

    public StringBuilder convert(MessageContext messageContext, String fromMediaType,
                                 String toMediaType) {
        logger.warn("Must be overridden.");
        return new StringBuilder("");
    }
}
