/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.impl.alertmgt;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API Store alert management class.
 * */
public class StoreAlertConfigurator extends AlertConfigurator {

    private static Log log = LogFactory.getLog(StoreAlertConfigurator.class);
    private ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

    @Override
    public void subscribe(String userName, List<String> emailsList, List<AlertTypeDTO> alertTypeDTOList)
            throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Subscribing user: " + userName + "to alert types");
        }
        String emails = StringUtils.join(emailsList, ",");
        Map<String, String> alertTypesMap = AlertMgtUtils.alertTypesToMap(alertTypeDTOList);

        if (log.isDebugEnabled()) {
            log.debug("Persisting subscribing alert types in database.");
        }

        String query =
                "select '" + userName + "' as userId, '" + alertTypesMap.get("names") + "' as alertTypes, '" + emails
                        + "' as emails, true as isSubscriber, false as isPublisher, "
                        + "false as isAdmin update or insert into ApimAlertStakeholderInfo "
                        + "set ApimAlertStakeholderInfo.userId = userId, "
                        + "ApimAlertStakeholderInfo.alertTypes = alertTypes , "
                        + "ApimAlertStakeholderInfo.emails = emails , "
                        + "ApimAlertStakeholderInfo.isSubscriber = isSubscriber, "
                        + "ApimAlertStakeholderInfo.isPublisher = isPublisher, "
                        + "ApimAlertStakeholderInfo.isAdmin = isAdmin on "
                        + "ApimAlertStakeholderInfo.userId == userId and "
                        + "ApimAlertStakeholderInfo.isSubscriber == isSubscriber";

        APIUtil.executeQueryOnStreamProcessor(AlertMgtConstants.APIM_STAKEHOLDER_ALERT_APP, query);
        apiMgtDAO.addAlertTypesConfigInfo(userName, emails, alertTypesMap.get("ids"), AlertMgtConstants.STORE_AGENT);
    }

    @Override
    public void unsubscribe(String userName) throws APIManagementException {

        apiMgtDAO.unSubscribeAlerts(userName, AlertMgtConstants.STORE_AGENT);
        String query = "delete ApimAlertStakeholderInfo on ApimAlertStakeholderInfo.userId == '" + userName + "' and "
                + "ApimAlertStakeholderInfo.isSubscriber == true";
        APIUtil.executeQueryOnStreamProcessor(AlertMgtConstants.APIM_STAKEHOLDER_ALERT_APP, query);

    }

    @Override
    public void addAlertConfiguration(String userName, String alertName, Map<String, String> configProperties)
            throws APIManagementException {

        String applicationId = configProperties.get(AlertMgtConstants.APPLICATION_ID_KEY);
        String apiName = configProperties.get(AlertMgtConstants.API_NAME_KEY);
        String apiVersion = configProperties.get(AlertMgtConstants.API_VERSION_KEY);
            String thresholdRequestCountPerMin = configProperties.get(AlertMgtConstants.REQUEST_COUNT_KEY);
        String query =
                "select '" + applicationId + "' as applicationId, '" + userName + "' as subscriber, '" + apiName
                        + "' as apiName, '" + apiVersion + "' as apiVersion, "
                        + Integer.valueOf(thresholdRequestCountPerMin)
                        + " as thresholdRequestCountPerMin update or insert into ApiSubAlertConf "
                        + "set ApiSubAlertConf.thresholdRequestCountPerMin = thresholdRequestCountPerMin "
                        + "on ApiSubAlertConf.applicationId == applicationId and "
                        + "ApiSubAlertConf.subscriber == subscriber and "
                        + "ApiSubAlertConf.apiName == apiName and ApiSubAlertConf.apiVersion == apiVersion";
        APIUtil.executeQueryOnStreamProcessor(AlertMgtConstants.APIM_ALERT_CONFIG_APP, query);
    }

    @Override
    public List<Map<String, String>> getAlertConfiguration(String userName, String alertName)
            throws APIManagementException {

        String query = "from ApiSubAlertConf on subscriber == '" + userName + "' select applicationId ,apiName , "
                + "apiVersion, thresholdRequestCountPerMin;";
        JSONObject result = APIUtil.executeQueryOnStreamProcessor(AlertMgtConstants.APIM_ALERT_CONFIG_APP, query);
        List<Map<String, String>> alertConfigList = new ArrayList<>();
        if (result != null && result.get("records") != null) {
            JSONArray alertConfigs = (JSONArray) result.get("records");
            for (Object config : alertConfigs) {
                JSONArray alertConfig = (JSONArray) config;
                Map<String, String> configProperties = new HashMap<>();
                configProperties.put(AlertMgtConstants.APPLICATION_ID_KEY, (String)alertConfig.get(0));
                configProperties.put(AlertMgtConstants.API_NAME_KEY, (String)alertConfig.get(1));
                configProperties.put(AlertMgtConstants.API_VERSION_KEY, (String)alertConfig.get(2));
                configProperties.put(AlertMgtConstants.REQUEST_COUNT_KEY, String.valueOf(alertConfig.get(3)));
                alertConfigList.add(configProperties);
            }
        }
        return alertConfigList;
    }

    @Override
    public void removeAlertConfiguration(String userName, String alertName, Map<String, String> configProperties)
            throws APIManagementException {

        String applicationId = configProperties.get(AlertMgtConstants.APPLICATION_ID_KEY);
        String apiName = configProperties.get(AlertMgtConstants.API_NAME_KEY);
        String apiVersion = configProperties.get(AlertMgtConstants.API_VERSION_KEY);

        String query = "delete ApiSubAlertConf on ApiSubAlertConf.applicationId == '"
                + applicationId + "' and ApiSubAlertConf.apiName == '" + apiName
                + "' and ApiSubAlertConf.subscriber == '" + userName
                + "' and ApiSubAlertConf.apiVersion == '" + apiVersion + "'";
        APIUtil.executeQueryOnStreamProcessor(AlertMgtConstants.APIM_ALERT_CONFIG_APP, query);
    }
}
