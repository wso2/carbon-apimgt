/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.rest.api.store.internal;

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
import org.wso2.carbon.datasource.core.api.DataSourceService;

import javax.naming.Context;
import javax.naming.NamingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component(
        name = "oorg.wso2.carbon.apimgt.rest.api.store",
        immediate = true
)
public class StoreComponent {

    private static final Logger log = LoggerFactory.getLogger(StoreComponent.class);

    @Activate
    public void start(BundleContext bundleContext) {

    }

    @Reference(
            name = "org.wso2.carbon.datasource.DataSourceService",
            service = DataSourceService.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "unregisterDataSourceService"
    )
    protected void onDataSourceServiceReady(DataSourceService service) {
        //this is required to enforce a dependency on datasources
    }

    @Reference(
            name = "org.wso2.carbon.datasource.jndi",
            service = JNDIContextManager.class,
            cardinality = ReferenceCardinality.AT_LEAST_ONE,
            policy = ReferencePolicy.DYNAMIC,
            unbind = "onJNDIUnregister"
    )
    protected void onJNDIReady(JNDIContextManager service) {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            Context ctx = service.newInitialContext();
            HikariDataSource dataSource = (HikariDataSource) ctx.lookup("java:comp/env/jdbc/test");
            conn = dataSource.getConnection();
            ps = conn.prepareStatement("SELECT * FROM abc");
            rs = ps.executeQuery();

            while (rs.next()) {
                log.info(rs.getString("a") + ">>>" + rs.getString("b"));
            }
        } catch (NamingException e) {
            log.info("Error occurred while jndi lookup", e);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    protected void onJNDIUnregister(JNDIContextManager jndiContextManager) {
        log.info("Unregistering data sources sample");
    }

    protected void unregisterDataSourceService(DataSourceService dataSourceService) {
        log.info("Unregistering data sources sample");
    }

}