/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.synapse.commons.datasource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.SynapseCommonsException;
import org.apache.synapse.commons.jmx.MBeanRegistrar;
import org.apache.synapse.commons.jmx.MBeanRepository;

import java.util.HashMap;
import java.util.Map;

/**
 * Keeps DatasourceMBeans
 */
public class DatasourceMBeanRepository implements MBeanRepository {

    private final static Log log = LogFactory.getLog(DatasourceMBeanRepository.class);

    private final Map<String, DBPoolView> dataSourcesMBeans
            = new HashMap<String, DBPoolView>();
    private final static DatasourceMBeanRepository DATASOURCE_MBEAN_REPOSITORY
            = new DatasourceMBeanRepository();
    private final static String MBEAN_CATEGORY_DATABASE_CONNECTION_POOL
            = "DatabaseConnectionPool";

    private DatasourceMBeanRepository() {
    }

    public static DatasourceMBeanRepository getInstance() {
        return DATASOURCE_MBEAN_REPOSITORY;
    }

    public void addMBean(String name, Object mBean) {

        if (name == null || "".equals(name)) {
            throw new SynapseCommonsException("DataSource MBean name cannot be found.", log);
        }

        if (mBean == null) {
            throw new SynapseCommonsException("DataSource MBean  cannot be found.", log);
        }

        if (!(mBean instanceof DBPoolView)) {
            throw new SynapseCommonsException("Given MBean instance is not matched" +
                    "with the expected MBean - 'DBPoolView'", log);
        }
        dataSourcesMBeans.put(name, (DBPoolView) mBean);
        MBeanRegistrar.getInstance().registerMBean(mBean,
                MBEAN_CATEGORY_DATABASE_CONNECTION_POOL, name);
    }

    public Object getMBean(String name) {
        if (name == null || "".equals(name)) {
            throw new SynapseCommonsException("DataSource MBean name cannot be found.", log);
        }
        return dataSourcesMBeans.get(name);
    }

    public void removeMBean(String name) {

        dataSourcesMBeans.remove(name);
        MBeanRegistrar.getInstance().unRegisterMBean(
                MBEAN_CATEGORY_DATABASE_CONNECTION_POOL, name);
    }

    public void clear() {

        if (!dataSourcesMBeans.isEmpty()) {
            log.info("UnRegistering DBPool MBeans");
            for (DBPoolView dbPoolView : dataSourcesMBeans.values()) {
                if (dbPoolView != null) {
                    removeMBean(dbPoolView.getName());
                }
            }
            dataSourcesMBeans.clear();
        }
    }
}
