/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.apache.synapse.endpoints;

import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clustering.Member;
import org.apache.axis2.clustering.management.GroupManagementAgent;
import org.apache.synapse.FaultHandler;

/**
 * This FaultHandler will try to resend the message to another member if an error occurs
 * while sending to some member. This is a failover mechanism
 */
public abstract class DynamicLoadbalanceFaultHandler extends FaultHandler {

    public abstract void setCurrentMember(Member currentMember);

    public abstract void setCurrentEp(Endpoint currentEp);

    public abstract void setTo(EndpointReference to);

}
