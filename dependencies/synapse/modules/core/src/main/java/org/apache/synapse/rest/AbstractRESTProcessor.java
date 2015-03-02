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

package org.apache.synapse.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;

/**
 * Abstract representation of an entity that can process REST messages. The caller can
 * first invoke the canProcess method of the processor to validate whether this processor
 * can process the given request or not.
 */
public abstract class AbstractRESTProcessor {

    protected Log log = LogFactory.getLog(getClass());

    protected String name;

    public AbstractRESTProcessor(String name) {
        this.name = name;
    }

    /**
     * Check whether this processor can handle the given request
     *
     * @param synCtx MessageContext of the message to be processed
     * @return true if the processor is suitable for handling the message
     */
    abstract boolean canProcess(MessageContext synCtx);

    /**
     * Process the given message through this processor instance
     *
     * @param synCtx MessageContext of the message to be processed
     */
    abstract void process(MessageContext synCtx);

    protected void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    protected void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }
}
