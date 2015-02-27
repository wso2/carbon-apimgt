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

package org.apache.synapse.message.store;

public final class Constants {
    /** Message ID of the Synapse Message Context that is passed in to the message store mediator */
    public static final String OriginalMessageID = "OrigMessageID";
    /** JMS Message Store */
    public static final int JMS_MS       = 0;
    /** JDBC Message Store */
    public static final int JDBC_MS      = 1;
    /** In Memory Message Store */
    public static final int INMEMORY_MS  = 2;

    /** Deprecated message store implementation class names**/
    public static final String DEPRECATED_INMEMORY_CLASS = "org.apache.synapse.message.store.InMemoryMessageStore";
    public static final String DEPRECATED_JMS_CLASS = "org.wso2.carbon.message.store.persistence.jms.JMSMessageStore";

}
