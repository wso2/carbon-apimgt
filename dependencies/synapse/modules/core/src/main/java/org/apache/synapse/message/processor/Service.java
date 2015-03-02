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

package org.apache.synapse.message.processor;

import org.apache.synapse.MessageContext;
import org.apache.synapse.message.MessageConsumer;
import org.quartz.JobExecutionContext;

public interface Service {
    /**
     * This method is used to initialize a service. Each time it fires this method get called. This ensures
     * all the configurations of the service is up to date.
     * @param jobExecutionContext is the execution environment of the message processors
     * @return true if the message processor is successfully initialized
     */
    boolean init(JobExecutionContext jobExecutionContext);

    /**
     * This method has the responsibility of fetching message from the message store.
     * @param msgConsumer is the associated message consumer of the message processor.
     * @return response message upon successful execution
     */
    MessageContext fetch(MessageConsumer msgConsumer);

    /**
     * This has the responsibility of dispatching the fetched message from the message store to the client.
     * Scenarios such as connection failures between client and service are handled by this method.
     * @param msgCtx is returned value of the fetch method.
     * @return true upon successful execution
     */
    boolean dispatch(MessageContext msgCtx);

    /**
     * This method terminates a running service immediately. Whatever state that of the service will not be
     * saved and it will be lost.
     * @return true upon successful execution
     */
    boolean terminate();
}
