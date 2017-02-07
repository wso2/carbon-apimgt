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
package org.wso2.carbon.apimgt.lifecycle.manager.core.impl;

import org.w3c.dom.Document;
import org.wso2.carbon.apimgt.lifecycle.manager.core.beans.LifecycleNode;
import org.wso2.carbon.apimgt.lifecycle.manager.core.exception.LifecycleException;
import org.wso2.carbon.apimgt.lifecycle.manager.core.util.LifecycleOperationUtil;
import org.wso2.carbon.apimgt.lifecycle.manager.core.util.LifecycleUtils;
import org.wso2.carbon.apimgt.lifecycle.manager.sql.beans.LifecycleHistoryBean;

import java.util.List;

/**
 * This class provides data related to life cycles
 */
public class LifecycleDataProvider {


    /**
     * This method provides set of operations performed to a particular lifecycle id.
     *
     * @param uuid  Lifecycle Id which requires history.
     * @return {@code List<LifecycleHistoryBean>} List of lifecycle history objects.
     * @throws LifecycleException
     */
    public static List<LifecycleHistoryBean> getLifecycleHistory(String uuid) throws LifecycleException {
        return LifecycleOperationUtil.getLifecycleHistoryFromId(uuid);
    }

    /**
     * This method provides set of lifecycle ids in a particular state.
     *
     * @param state                 Filtering state.
     * @param lcName                Name of the relevant lifecycle.
     * @return                      {@code List<LifecycleHistoryBean>} List of lifecycle ids in the given state.
     * @throws LifecycleException
     */
    public static List<String> getIdsFromState(String state, String lcName) throws LifecycleException {
        return LifecycleOperationUtil.getLifecycleIds(state, lcName);
    }

    /**
     * This method is used to read lifecycle config and provide state chart as graph in order to visually represent the
     * lifecycle config.
     *
     * @param lcName                          Name of the lifecycle.
     * @return Lifecycle config as a graph of states.
     * @throws LifecycleException
     */
    public static List<LifecycleNode> getLifecycleGraph(String lcName) throws LifecycleException {
        Document lcContent = LifecycleUtils.getLifecycleConfiguration(lcName);
        return LifecycleOperationUtil.buildLifecycleGraph(lcContent);
    }
}
