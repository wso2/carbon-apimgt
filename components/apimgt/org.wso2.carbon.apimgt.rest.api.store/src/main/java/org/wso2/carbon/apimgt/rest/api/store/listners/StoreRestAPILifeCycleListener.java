/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.rest.api.store.listners;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

@SuppressWarnings("unused")
public class StoreRestAPILifeCycleListener implements ServletContextListener {

    private static final Log log =
            LogFactory.getLog(StoreRestAPILifeCycleListener.class);


    /**
     * This method is here to do deployment specific tasks
     * If you need to generate tokens when app is deployed you may use this method to automate process.
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {

        log.info("API Manager publisher REST API web application deployed");

    }

    /**
     * This method is to handle web app context destroy operations.
     * If we need to invalidate sessions etc we should handle them here
     *
     * @param sce  ServletContextEvent
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("API Manager publisher REST API web application un deployed");
    }
}
