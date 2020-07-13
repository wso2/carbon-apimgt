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

package org.wso2.carbon.apimgt.impl.eventpublishers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.keymgt.ExpiredJWTCleaner;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapter;
import org.wso2.carbon.event.output.adapter.core.exception.ConnectionUnavailableException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.exception.TestConnectionNotSupportedException;

import java.util.Map;

public class TokenRevocationEventRDBMSPublisher implements OutputEventAdapter {

    private static final Log log = LogFactory.getLog(TokenRevocationEventRDBMSPublisher.class);

    @Override
    public void init() throws OutputEventAdapterException {

    }

    @Override
    public void testConnect() throws TestConnectionNotSupportedException, ConnectionUnavailableException {

    }

    @Override
    public void connect() throws ConnectionUnavailableException {

    }

    @Override
    public void publish(Object o, Map<String, String> map) throws ConnectionUnavailableException {

        if (o instanceof Map) {
            Map<String, Object> event = (Map<String, Object>) o;
            ApiMgtDAO instance = ApiMgtDAO.getInstance();
            String eventId = (String) event.get(APIConstants.NotificationEvent.EVENT_ID);
            String revokedToken = (String) event.get(APIConstants.REVOKED_TOKEN_KEY);
            Object type = event.get(APIConstants.REVOKED_TOKEN_TYPE);
            Long expiryTime = (Long) event.get(APIConstants.REVOKED_TOKEN_EXPIRY_TIME);
            int tenantId = (int) event.get(APIConstants.NotificationEvent.TENANT_ID);
            if (APIConstants.JWT.equals(type) || APIConstants.API_KEY_AUTH_TYPE.equals(type)) {
                try {
                    instance.addRevokedJWTSignature(eventId,revokedToken, (String) type, expiryTime, tenantId);
                    new Thread(new ExpiredJWTCleaner()).start();
                } catch (APIManagementException e) {
                    log.error("Error while persisting revoked token", e);
                }

            }
        }

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean isPolled() {

        return false;
    }
}
