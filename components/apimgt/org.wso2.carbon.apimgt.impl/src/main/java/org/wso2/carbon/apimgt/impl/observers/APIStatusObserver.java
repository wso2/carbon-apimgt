/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.impl.observers;

import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIStatus;

/**
 * One or more implementations of this interface can be registered with the APIProvider
 * to receive and handle API status update events.
 */
public interface APIStatusObserver {

    /**
     * This event is fired every time the status of an API is changed through the APIProvider
     * interface. Note that by the time this event is fired, the status field on the API object
     * is already modified and hence any calls to api.getStatus() will return the latest status
     * (i.e current). The return value of the method determines whether the subsequent observers
     * in the chain should be notified or not. In situation where publishing to gateway is
     * also handled by the APIProvider, this event gets fired before the actual publishing
     * operations.
     *
     * @param previous Previous status of the API (i.e status before the update)
     * @param current Current status of the API
     * @param api API instance on which the update operation is performed
     * @return true if the next observer in the chain should be invoked, or false to prevent
     *         notifying other observers
     *
     */
    public boolean statusChanged(APIStatus previous, APIStatus current, API api);

}
