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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.util.Properties;

/**
 * Utility class to locate DataSources from a JNDI tree
 */
public class DataSourceFinder {

    private static Log log = LogFactory.getLog(DataSourceFinder.class);

    /**
     * Find a DataSource using the given name and JNDI environment properties
     *
     * @param dsName  Name of the DataSource to be found
     * @param jndiEnv JNDI environment properties
     * @return DataSource if found , otherwise null
     */
    public static DataSource find(String dsName, Properties jndiEnv) {

        try {
            Context context;
            if (jndiEnv == null) {
                context = new InitialContext();
            } else {
                context = new InitialContext(jndiEnv);
            }
            return find(dsName, context);

        } catch (NamingException e) {
            throw new SynapseCommonsException("Error looking up DataSource : " + dsName +
                    " using JNDI properties : " + jndiEnv, e, log);
        }
    }

    /**
     * Find a DataSource using the given name and naming context
     *
     * @param dsName  Name of the DataSource to be found
     * @param context Naming Context
     * @return DataSource if found , otherwise null
     */
    public static DataSource find(String dsName, Context context) {

        try {
            Object dataSourceO = context.lookup(dsName);
            if (dataSourceO != null && dataSourceO instanceof DataSource) {
                return (DataSource) dataSourceO;
            } else {
                throw new SynapseCommonsException("DataSource : " + dsName + " not found " +
                        "when looking up using JNDI properties : " + context.getEnvironment(), log);
            }

        } catch (NamingException e) {
            throw new SynapseCommonsException("Error looking up DataSource : " + dsName
                    + " using JNDI" + " properties : " + context, e, log);
        }
    }
}
