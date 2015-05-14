/*
 *   Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 *
 */
package org.wso2.carbon.apimgt.impl.auth.manager;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.StackObjectPool;
import org.wso2.carbon.apimgt.api.APIManagementException;

public class StandaloneAuthorizationClientFactory extends AuthorizationManagerClientFactory {

    private ObjectPool clientPool;

    public StandaloneAuthorizationClientFactory() {
        this.clientPool = new StackObjectPool(new BasePoolableObjectFactory() {
            @Override
            public Object makeObject() throws Exception {
                return new StandaloneAuthorizationManagerClient();
            }
        });
    }

    @Override
    public AuthorizationManagerClient getAuthorizationManagerClient() throws APIManagementException {
        try {
            return (StandaloneAuthorizationManagerClient) clientPool.borrowObject();
        } catch (Exception e) {
            throw new APIManagementException("Error occurred while borrowing a StandaloneAuthorizationClient from " +
                    "the object pool", e);
        }
    }

    @Override
    public void releaseAuthorizationManagerClient(AuthorizationManagerClient client) throws APIManagementException {
        try {
            clientPool.returnObject(client);
        } catch (Exception e) {
            throw new APIManagementException("Error occurred while returning the StandaloneAuthorizationClient " +
                    "to the object pool", e);
        }
    }

}
