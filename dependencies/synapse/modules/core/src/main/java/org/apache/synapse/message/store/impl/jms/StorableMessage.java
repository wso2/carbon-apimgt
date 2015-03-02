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

package org.apache.synapse.message.store.impl.jms;

import java.io.Serializable;

/**
 * This represents the final JMS message that will be saved in the JMS queue.
 */
public class StorableMessage implements Serializable {
    private Axis2Message axis2message;

    private SynapseMessage synapseMessage;

    private int priority = 4;

    public Axis2Message getAxis2message() {
        return axis2message;
    }

    public void setAxis2message(Axis2Message axis2message) {
        this.axis2message = axis2message;
    }

    public SynapseMessage getSynapseMessage() {
        return synapseMessage;
    }

    public void setSynapseMessage(SynapseMessage synapseMessage) {
        this.synapseMessage = synapseMessage;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
