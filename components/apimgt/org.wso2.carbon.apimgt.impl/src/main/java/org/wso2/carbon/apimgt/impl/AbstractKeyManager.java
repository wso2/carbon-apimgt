/*
 *
 *   Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * /
 */

package org.wso2.carbon.apimgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.KeyManager;
import org.wso2.carbon.apimgt.api.model.OAuthApplicationInfo;

import java.util.Map;

/**
 * Mostly common features of  keyManager implementations will be handle here.
 * This class should be extended by Key manager implementation class.
 */
public abstract class AbstractKeyManager implements KeyManager {
    private static Log log = LogFactory.getLog(AbstractKeyManager.class);

    /**
     * This method will accept json String and will do the json parse will set oAuth application properties to OAuthApplicationInfo object.
     *
     * @param jsonInput this jsonInput will contain set of oAuth application properties.
     * @return OAuthApplicationInfo object will be return.
     * @throws APIManagementException
     */
    public OAuthApplicationInfo buildFromJSON(String jsonInput) throws APIManagementException {
        //initiate json parser.
        JSONParser parser = new JSONParser();
        JSONObject jsonObject;

        try {
            //parse json String
            jsonObject = (JSONObject) parser.parse(jsonInput);
            if (jsonObject instanceof Map) {
                //create a map to hold json parsed objects.
                Map<String, Object> params = (Map) jsonObject;
                //initiate OAuthApplicationInfo object.
                OAuthApplicationInfo info = new OAuthApplicationInfo();
                //set client Id
                if ((String) params.get("client_id") != null) {
                    info.setClientId((String) params.get("client_id"));
                }
                //copy all params map in to OAuthApplicationInfo's Map object.
                info.putAll(params);
                return info;
            }
        } catch (ParseException e) {
            handleException("Error occurred while parsing JSON String", e);
        }
        return null;
    }

    /**
     * common method to throw exceptions.
     *
     * @param msg this parameter contain error message that we need to throw.
     * @param e   Exception object.
     * @throws APIManagementException
     */
    private void handleException(String msg, Exception e) throws APIManagementException {
        log.error(msg, e);
        throw new APIManagementException(msg, e);
    }
}
