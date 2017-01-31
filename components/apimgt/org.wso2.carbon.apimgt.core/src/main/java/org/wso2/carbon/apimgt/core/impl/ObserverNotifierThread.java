/*
 *
 *   Copyright (c) ${date}, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.impl;

import org.wso2.carbon.apimgt.core.api.APIMObservable;
import org.wso2.carbon.apimgt.core.models.Component;
import org.wso2.carbon.apimgt.core.models.Event;

/**
 * Thread used to notify the observers whenever an event occurs in an Observable
 */
public class ObserverNotifierThread implements Runnable {

    private Component component;
    private Event event;
    private String username;
    private Thread threadObj;
    private APIMObservable observable;

    public ObserverNotifierThread(Component component, Event event, String username, APIMObservable observable) {
        this.component = component;
        this.event = event;
        this.username = username;
        this.observable = observable;
    }

    @Override
    public void run() {
        if (observable != null) {
            observable.notifyObservers(component, event, username);
        }
    }

    public void start() {
        if (threadObj == null) {
            threadObj = new Thread(this);
            threadObj.start();
        }
    }
}
