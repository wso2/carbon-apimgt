/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.lifecycle.manager.core.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * This class provides single instance in order to hold references to other services used by
 * lifecycle service component.
 */
public class ServiceReferenceHolder {
    private static Logger  log = LoggerFactory.getLogger(ServiceReferenceHolder.class);
    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();
    //private  ConfigurationContextService contextService;
    //private JNDIContextManager jndiContextManager;
    private ServiceReferenceHolder() {

    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    /*public JNDIContextManager getJndiContextManager() {
        return jndiContextManager;
    }

    public void setJndiContextManager(JNDIContextManager jndiContextManager) {
        this.jndiContextManager = jndiContextManager;
    }*/

    /*public ConfigurationContextService getContextService() {
        return this.contextService;
    }

    public  void setContextService(ConfigurationContextService contextService) {
        this.contextService = contextService;
    }

*/
}
