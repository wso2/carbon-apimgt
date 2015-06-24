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

package org.apache.synapse.transport.passthru.jmx;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.Set;

public class MBeanRegistrar {

    private static final MBeanRegistrar ourInstance = new MBeanRegistrar();
    private static final Log log = LogFactory.getLog(MBeanRegistrar.class);

    public static MBeanRegistrar getInstance() {
        return ourInstance;
    }

    private MBeanRegistrar() {
    }

    public void registerMBean(Object mBeanInstance, String category, String id) throws AxisFault {
        assertNull(mBeanInstance, "MBean instance is null");
        assertNull(category, "MBean instance category is null");
        assertNull(id, "MBean instance name is null");
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName name = new ObjectName(getObjectName(category, id));
            Set set = mbs.queryNames(name, null);
            if (set != null && set.isEmpty()) {
                mbs.registerMBean(mBeanInstance, name);
            } else {
                mbs.unregisterMBean(name);
                mbs.registerMBean(mBeanInstance, name);
            }
        } catch (Exception e) {
            log.warn("Error registering a MBean with name ' " + id +
                    " ' and category name ' " + category + "' for JMX management", e);
        }
    }

    public void unRegisterMBean(String category, String id) {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName objName = new ObjectName(getObjectName(category, id));
            if (mbs.isRegistered(objName)) {
                mbs.unregisterMBean(objName);
            }
        } catch (Exception e) {
            log.warn("Error un-registering a  MBean with name ' " + id +
                    " ' and category name ' " + category + "' for JMX management", e);
        }
    }

    private String getObjectName(String category, String id) {

        String jmxAgentName = System.getProperty("jmx.agent.name");
        if (jmxAgentName == null || "".equals(jmxAgentName)) {
            jmxAgentName = "org.apache.synapse";
        }
        return jmxAgentName + ":Type=" + category + ",Name=" + id;
    }

    private void assertNull(String name, String msg) throws AxisFault {
        if (name == null || "".equals(name)) {
            handleException(msg);
        }
    }

    private void assertNull(Object object, String msg) throws AxisFault {
        if (object == null) {
            handleException(msg);
        }
    }

    private static void handleException(String msg) throws AxisFault {
        log.error(msg);
        throw new AxisFault(msg);
    }

}
