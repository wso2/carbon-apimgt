/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.gateway.conditiongroup;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.dto.ConditionGroupDTO;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  Singleton which stores the condition groups map
 */
public class ConditionGroupsDataHolder {

    private static final Log log = LogFactory.getLog(ConditionGroupsDataHolder.class);
    private static Map<String, ConditionGroupDTO[]> conditionGroupsMap = new ConcurrentHashMap<>();
    private static ConditionGroupsDataHolder instance = new ConditionGroupsDataHolder();

    /**
     * Adds a given key,value pair to the condition groups map.
     * @param key key to be added.
     * @param value value to be added.
     */
    public void addConditionGroupToMap(String key, ConditionGroupDTO value) {
        if (key != null && value != null) {
            ConditionGroupDTO[] conditionGroupDTOS = conditionGroupsMap.get(key);
            if (conditionGroupDTOS == null) {
                conditionGroupDTOS = new ConditionGroupDTO[1];
            } else {
                conditionGroupDTOS = Arrays.copyOf(conditionGroupDTOS, conditionGroupDTOS.length + 1);
            }
            conditionGroupDTOS[conditionGroupDTOS.length - 1] = value;
            log.debug("Adding condition group key, value pair to the map :" + key + " , " + value);
            conditionGroupsMap.put(key, conditionGroupDTOS);
        }
    }


    private ConditionGroupsDataHolder() {

    }

    /**
     * Fetches the condition groups map.
     * @return
     */
    Map<String, ConditionGroupDTO[]> getConditionGroupsMap() {
        return conditionGroupsMap;
    }

    public void updatePolicyConditionGroup(String key, ConditionGroupDTO[] conditionGroupsDTOS) {
        if (key != null) {
            conditionGroupsMap.put(key, conditionGroupsDTOS);
        }
    }

    /**
     * This method can be used to get the singleton instance of this class.
     * @return the singleton instance.
     */
    public static ConditionGroupsDataHolder getInstance() {
        return instance;
    }
}