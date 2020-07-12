/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.notification;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.synapse.commons.json.JsonUtil;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.keymgt.KeyManagerEventHandler;

/**
 *  Abstract Implementation of KeyManagerEventHandler.
 */
public abstract class AbstractKeyManagerEventHandler implements KeyManagerEventHandler {

    public abstract boolean handleEvent(String event);

    @Override
    public boolean handleEvent(OMElement event) throws APIManagementException {

        if (JsonUtil.hasAJsonPayload(event)) {
            try {
                StringBuilder content = JsonUtil.toJsonString(event);
                return handleEvent(content.toString());
            } catch (AxisFault axisFault) {
                throw new APIManagementException("Error while converting payload to json", axisFault);
            }
        } else {
            String text = event.getText();
            return handleEvent(text);
        }
    }
}
