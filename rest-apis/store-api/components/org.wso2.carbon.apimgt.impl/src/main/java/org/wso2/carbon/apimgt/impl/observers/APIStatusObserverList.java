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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;

import java.util.ArrayList;
import java.util.List;

public class APIStatusObserverList {

    private static final Log log = LogFactory.getLog(APIStatusObserverList.class);

    private static final APIStatusObserverList instance = new APIStatusObserverList();

    private List<APIStatusObserver> observers = new ArrayList<APIStatusObserver>();
    private boolean initialized = false;

    private APIStatusObserverList() {
        log.debug("Creating the singleton APIStatusObserverList instance");
    }

    public static APIStatusObserverList getInstance() {
        return instance;
    }

    public void init(APIManagerConfiguration config) {
        if (initialized) {
            log.warn("Attempt to reinitialize APIStatusObserverList - Skipping");
            return;
        }
        
        List<String> values = config.getProperty(APIConstants.OBSERVER);
        if (values != null) {
            for (String value : values) {
                APIStatusObserver observer = createObserver(value);
                if (observer != null) {
                    observers.add(observer);
                }
            }
        }
        initialized = true;
    }
    
    private APIStatusObserver createObserver(String className) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing the APIStatusObserver from class: " + className);
        }

        try {
            Class clazz = getClass().getClassLoader().loadClass(className);
            return (APIStatusObserver) clazz.newInstance();
        } catch (Exception e) {
            log.error("Error while initializing the observer from class: " + className, e);
            return null;
        }
    }

    public void notifyObservers(APIStatus previous, APIStatus current, API api) {
        for (APIStatusObserver observer : observers) {
            boolean proceed = observer.statusChanged(previous, current, api);
            if (!proceed) {
                break;
            }
        }
    }

}
