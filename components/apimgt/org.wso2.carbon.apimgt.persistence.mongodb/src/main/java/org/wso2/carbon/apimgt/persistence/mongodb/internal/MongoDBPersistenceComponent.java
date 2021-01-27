/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.persistence.mongodb.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.apimgt.persistence.APIPersistence;
import org.wso2.carbon.apimgt.persistence.mongodb.MongoDBPersistenceImpl;

@Component(
        name = "org.wso2.carbon.apimgt.persistence.mongodb",
        immediate = true)
public class MongoDBPersistenceComponent {


    private ServiceRegistration serviceRegistration = null;
    private static final Log log = LogFactory.getLog(MongoDBPersistenceImpl.class);

    @Activate
    protected void activate(ComponentContext context) {
        serviceRegistration = context.getBundleContext().registerService(APIPersistence.class.getName(),
                new MongoDBPersistenceImpl(), null);
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
        if (serviceRegistration != null) {
            serviceRegistration.unregister();
        }
        if (log.isDebugEnabled()) {
            log.info("Mongodb service deactivated.");
        }
    }
}
