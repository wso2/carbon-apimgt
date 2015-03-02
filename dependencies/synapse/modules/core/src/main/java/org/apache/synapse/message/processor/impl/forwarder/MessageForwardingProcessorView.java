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
package org.apache.synapse.message.processor.impl.forwarder;

import org.apache.synapse.SynapseException;
import java.util.List;

public class MessageForwardingProcessorView implements MessageForwardingProcessorViewMBean {

    private ScheduledMessageForwardingProcessor processor;

    public MessageForwardingProcessorView(ScheduledMessageForwardingProcessor processor)
            throws Exception {

        if (processor != null) {
            this.processor = processor;
        } else {
            throw new SynapseException("Error , Can not create Message Forwarding Processor " +
                    "view with null " + "Message Processor");
        }
    }

    public void resendAll() throws Exception { throw new Exception("Manual operations are not supported!"); }

    public void deleteAll() throws Exception { throw new Exception("Manual operations are not supported!"); }

    public List<String> messageIdList() throws Exception { throw new Exception("Manual operations are not supported!"); }

    public void resend(String messageID) throws Exception { throw new Exception("Manual operations are not supported!"); }

    public void delete(String messageID) throws Exception { throw new Exception("Manual operations are not supported!"); }

    public String getEnvelope(String messageID) throws Exception { throw new Exception("Manual operations are not supported!"); }

    public int getSize() {
        // This function is not supported anymore.
        return -1;
    }

    public boolean isActive() {
        assert processor != null;
        return processor.isActive();
    }

    public void activate() {
        assert processor != null;
        processor.activate();
    }

    public void deactivate() {
        assert processor != null;
        processor.deactivate();
    }
}
