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

package org.apache.synapse.commons.beanstalk.enterprise;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * MBean implementation class that enables monitoring/managing beanstalks via JMX.
 */
public class EnterpriseBeanstalkView implements EnterpriseBeanstalkViewMBean {

    private EnterpriseBeanstalk beanstalk;

    EnterpriseBeanstalkView(EnterpriseBeanstalk beanstalk) {
        this.beanstalk = beanstalk;
    }

    public String getBeanstalkName() {
        return beanstalk.getName();
    }

    public int getCachedStatelessStubCount() {
        return beanstalk.getStatelessBeans().size();
    }

    public int getCachedStatefulStubCount() {
        return beanstalk.getStatefulBeans().size();
    }

    public Map getStatelessStubCacheLastAccessTimes() {
        return getLastAccessTimesMap(beanstalk.getStatelessBeans());
    }

    public Map getStatefulStubCacheLastAccessTimes() {
        return getLastAccessTimesMap(beanstalk.getStatefulBeans());
    }

    private Map getLastAccessTimesMap(Map<String, CacheEntry> beanMap) {
        Map<String, Date> results = new HashMap<String, Date>();
        for (Map.Entry<String, CacheEntry> entry : beanMap.entrySet()) {
            results.put(entry.getKey(), new Date(entry.getValue().getLastAccessTime()));
        }
        return results;
    }

    public void cleanExpiredStubsNow() throws Exception {
        beanstalk.removeExpiredBeans();
    }

    public void removeStatelessStub(String className) {
        beanstalk.removeEnterpriseBean(className, null);
    }

    public void removeStatefulStub(String className, String sessionId) {
        beanstalk.removeEnterpriseBean(className, sessionId);
    }
}
