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

import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.notification.event.TokenRevocationEvent;

import java.util.List;
import java.util.Map;

/**
 * This class implements to handle Default key management related notification events.
 */
public class DefaultKeyManagerEventHandlerImpl extends AbstractKeyManagerEventHandler {

    @Override
    public boolean handleEvent(String event, Map<String, List<String>> headers) throws APIManagementException {

        if (StringUtils.isNotEmpty(event) && event.contains(APIConstants.NotificationEvent.TOKEN_REVOCATION_EVENT)) {
            handleTokenRevocationEvent(event);
        }
        return true;
    }

    @Override
    public String getType() {

        return APIConstants.KeyManager.DEFAULT_KEY_MANAGER_TYPE;
    }

    private boolean handleTokenRevocationEvent(String event) throws APIManagementException {

        TokenRevocationEvent tokenRevocationEvent = new Gson().fromJson(event, TokenRevocationEvent.class);
        handleTokenRevocationEvent(tokenRevocationEvent);
        return true;
    }
}
