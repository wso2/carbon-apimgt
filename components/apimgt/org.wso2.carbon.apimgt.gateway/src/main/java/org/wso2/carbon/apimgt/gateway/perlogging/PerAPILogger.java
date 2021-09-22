/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.gateway.perlogging;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.wso2.carbon.apimgt.gateway.handlers.LogsHandler;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.perlog.PerAPILogService;
import org.wso2.carbon.databridge.commons.Event;

/**
 * This class is responsible for publishing data to TM, updating, deleting API related data in local entry
 */
public class PerAPILogger implements PerAPILogService {

    private static PerAPILogService perAPILogService = new PerAPILogger();
    private static String streamId = "org.wso2.apimgt.perapi.log.stream:1.0.0";


    private PerAPILogger() {
    }

    public static PerAPILogService getInstance() {
        if (perAPILogService == null) {
            perAPILogService = new PerAPILogger();
        }
        return perAPILogService;
    }

    @Override public void publishLogAPIData(String context, String value) {
        Object[] objects = new Object[] { context, value };
        Event perapilogmessage = new Event(streamId, System.currentTimeMillis(), null, null, objects);
        ServiceReferenceHolder.getInstance().getThrottleDataPublisher().getDataPublisher().tryPublish(perapilogmessage);
    }

    @Override public Map<String, String> getLogData() {
        return LogsHandler.getLogData();
    }

    @Override public String getLogData(String context) {
        return LogsHandler.getLogData(context);
    }

    @Override public void syncLocalAPILogDetailsMap(Map<String, Object> map) {
        LogsHandler.syncAPILogData(map);
    }

}
