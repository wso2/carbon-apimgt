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
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * API Store alert management class.
 * */
public class StoreAlertConfigurator implements AlertConfigurator {

    private final String STAKEHOLDER = "subscriber";
    private final String APIM_ALERT_STAKEHOLDER_APP = "APIM_ALERT_STAKEHOLDER";
    private final String APIM_ALERT_CONFIG_APP = "APIM_ALERT_CONFIGURATION";
    private final String APPLICATION_ID_KEY = "applicationId";
    private final String API_NAME_KEY = "apiName";
    private final String API_VERSION_KEY = "apiVersion";
    private final String PROPERTY_KEY = "thresholdRequestCountPerMin";
    private ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

    @Override
    public List<AlertTypeDTO> getSupportedAlertTypes() throws APIManagementException {
        Map<Integer, String> supportedAlertTypes = APIUtil.getAllAlertTypeByStakeHolder(STAKEHOLDER);
        return AlertMgtUtils.toAlertTypeDTO(supportedAlertTypes);
    }

    @Override
    public List<String> getSubscribedEmailAddresses(String userName) throws APIManagementException {
        return APIUtil.retrieveSavedEmailList(userName, STAKEHOLDER);
    }

    @Override
    public List<Integer> getSubscribedAlerts(String userName) throws APIManagementException {
        return APIUtil.getSavedAlertTypesIdsByUserNameAndStakeHolder(userName, STAKEHOLDER);
    }

    @Override
    public void subscribe(String userName, List<String> emailsList, List<AlertTypeDTO> alertTypeDTOList)
            throws APIManagementException {
        String emails = StringUtils.join(emailsList, ",");
        Map<String, String> alertTypesMap = AlertMgtUtils.alertTypesToMap(alertTypeDTOList);

        apiMgtDAO.addAlertTypesConfigInfo(userName, emails, alertTypesMap.get("ids"), STAKEHOLDER);

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
        APIUtil.executeQueryOnStreamProcessor(APIM_ALERT_STAKEHOLDER_APP, query);
    }

    @Override
    public void unsubscribe(String userName) throws APIManagementException {

        apiMgtDAO.unSubscribeAlerts(userName, STAKEHOLDER);
        String query = "delete ApimAlertStakeholderInfo  on ApimAlertStakeholderInfo.userId == '" + userName + "' and "
                + "ApimAlertStakeholderInfo.isSubscriber == true";
        APIUtil.executeQueryOnStreamProcessor(APIM_ALERT_STAKEHOLDER_APP, query);

    }

    @Override
    public void addAlertConfiguration(String userName, String alertName, Properties configProperties)
            throws APIManagementException {
        String applicationId = configProperties.getProperty(APPLICATION_ID_KEY);
        String apiName = configProperties.getProperty(API_NAME_KEY);
        String apiVersion = configProperties.getProperty(API_VERSION_KEY);
        String thresholdRequestCountPerMin = configProperties.getProperty(PROPERTY_KEY);
        String query =
                "select '" + applicationId + "' as applicationId, '" + userName + "' as subscriber, '" + apiName
                        + "' as apiName,' " + apiVersion + "' as apiVersion, "
                        + Integer.valueOf(thresholdRequestCountPerMin)
                        + " as thresholdRequestCountPerMin update or insert into ApiSubAlertConf "
                        + "set ApiSubAlertConf.thresholdRequestCountPerMin = thresholdRequestCountPerMin "
                        + "on ApiSubAlertConf.applicationId == applicationId and "
                        + "ApiSubAlertConf.subscriber == subscriber and "
                        + "ApiSubAlertConf.apiName == apiName and ApiSubAlertConf.apiVersion == apiVersion";
        APIUtil.executeQueryOnStreamProcessor(APIM_ALERT_CONFIG_APP, query);
    }

    @Override
    public List<Properties> getAlertConfiguration(String userName, String alertName) throws APIManagementException {
        String query = "from ApiSubAlertConf on subscriber == '" + userName + "' select applicationId ,apiName , "
                + "apiVersion, thresholdRequestCountPerMin;";
        JSONObject result = APIUtil.executeQueryOnStreamProcessor(APIM_ALERT_CONFIG_APP, query);
        List<Properties> alertConfigList = new ArrayList<>();
        if (result.get("records") != null) {
            JSONArray alertConfigs = (JSONArray) result.get("records");
            for (Object config : alertConfigs) {
                JSONArray alertConfig = (JSONArray) config;
                Properties configProperties = new Properties();
                configProperties.setProperty(APPLICATION_ID_KEY, (String)alertConfig.get(0));
                configProperties.setProperty(API_NAME_KEY, (String)alertConfig.get(1));
                configProperties.setProperty(API_VERSION_KEY, (String)alertConfig.get(2));
                configProperties.setProperty(PROPERTY_KEY, String.valueOf(alertConfig.get(3)));
                alertConfigList.add(configProperties);
            }
        }
        return alertConfigList;
    }

    @Override
    public void removeAlertConfiguration(String userName, String alertName, Properties configProperties)
            throws APIManagementException {

        String applicationId = configProperties.getProperty(APPLICATION_ID_KEY);
        String apiName = configProperties.getProperty(API_NAME_KEY);
        String apiVersion = configProperties.getProperty(API_VERSION_KEY);

        String query = "delete ApiSubAlertConf on ApiSubAlertConf.applicationId == '"
                + applicationId + "' and ApiSubAlertConf.apiName == '" + apiName
                + "' and ApiSubAlertConf.subscriber == '" + userName
                + "' and ApiSubAlertConf.apiVersion == '" + apiVersion + "'";
        JSONObject result = APIUtil.executeQueryOnStreamProcessor(APIM_ALERT_CONFIG_APP, query);
    }
}
