/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.dao.internal;

import com.zaxxer.hikari.HikariDataSource;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.jndi.JNDIContextManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.dao.impl.DAOUtil;

import javax.naming.Context;
import javax.naming.NamingException;

/**
 * Bundle activator component responsible for retrieving the JNDIContextManager OSGi service
 * and reading its datasource configuration
 */
@Component(
        name = "org.wso2.carbon.apimgt.dao",
        immediate = true
)
public class BundleActivator {

    private static final Logger log = LoggerFactory.getLogger(BundleActivator.class);
    private JNDIContextManager jndiContextManager;

    @Activate
    protected void start(BundleContext bundleContext) {
        try {
            Context ctx = jndiContextManager.newInitialContext();
            DAOUtil.initialize((HikariDataSource) ctx.lookup("java:comp/env/jdbc/WSO2AMDB"));
        } catch (NamingException e) {
            log.error("Error occurred while jndi lookup", e);
        }
    }


    @Reference(
            name = "org.wso2.carbon.datasource.jndi",
            service = JNDIContextManager.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "onJNDIUnregister"
    )
    protected void onJNDIReady(JNDIContextManager jndiContextManager) {
        this.jndiContextManager = jndiContextManager;

    }


    protected void onJNDIUnregister(JNDIContextManager jndiContextManager) {
        this.jndiContextManager = null;
    }


}
