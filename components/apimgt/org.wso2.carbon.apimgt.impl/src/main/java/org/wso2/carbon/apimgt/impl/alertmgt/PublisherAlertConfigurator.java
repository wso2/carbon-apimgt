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
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PublisherAlertConfigurator extends AlertConfigurator {

    private static final Log log = LogFactory.getLog(PublisherAlertConfigurator.class);
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
            log.debug("Persisting subscribing alert types " + alertTypesMap.get("ids") + "in database.");
        }

        String query =
                "select '" + userName + "' as userId, '" + alertTypesMap.get("names") + "' as alertTypes, '" + emails
                        + "' as emails, false as isSubscriber, true as isPublisher, "
                        + "false as isAdmin update or insert into ApimAlertStakeholderInfo "
                        + "set ApimAlertStakeholderInfo.userId = userId, "
                        + "ApimAlertStakeholderInfo.alertTypes = alertTypes , "
                        + "ApimAlertStakeholderInfo.emails = emails , "
                        + "ApimAlertStakeholderInfo.isSubscriber = isSubscriber, "
                        + "ApimAlertStakeholderInfo.isPublisher = isPublisher, "
                        + "ApimAlertStakeholderInfo.isAdmin = isAdmin on "
                        + "ApimAlertStakeholderInfo.userId == userId and "
                        + "ApimAlertStakeholderInfo.isPublisher == isPublisher";
        APIUtil.executeQueryOnStreamProcessor(AlertMgtConstants.APIM_STAKEHOLDER_ALERT_APP, query);
        apiMgtDAO.addAlertTypesConfigInfo(userName, emails, alertTypesMap.get("ids"),
                AlertMgtConstants.PUBLISHER_AGENT);
    }

    @Override
    public void unsubscribe(String userName) throws APIManagementException {
        apiMgtDAO.unSubscribeAlerts(userName, AlertMgtConstants.PUBLISHER_AGENT);
        String query = "delete ApimAlertStakeholderInfo on ApimAlertStakeholderInfo.userId == '" + userName + "' and "
                + "ApimAlertStakeholderInfo.isPublisher == true";
        APIUtil.executeQueryOnStreamProcessor(AlertMgtConstants.APIM_STAKEHOLDER_ALERT_APP, query);
    }

    @Override
    public void addAlertConfiguration(String userName, String alertName, Map<String, String> configProperties)
            throws APIManagementException {
        String apiName = configProperties.get(AlertMgtConstants.API_NAME_KEY);
        String apiVersion = configProperties.get(AlertMgtConstants.API_VERSION_KEY);
        String configPropertyName = AlertMgtConstants.alertTypeConfigMap.get(alertName);
        String configValue = configProperties.get(configPropertyName);
        String query = buildAddConfigQuery(userName, apiName, apiVersion, alertName, configValue);
        APIUtil.executeQueryOnStreamProcessor(AlertMgtConstants.APIM_ALERT_CONFIG_APP, query);
    }

    @Override
    public List<Map<String, String>> getAlertConfiguration(String userName, String alertName)
            throws APIManagementException {
        String configPropertyName = AlertMgtConstants.alertTypeConfigMap.get(alertName);
        String query = "from ApiCreatorAlertConfiguration on apiCreator=='" + userName + "' and "
                + configPropertyName + "!=0 select apiName,apiVersion,apiCreator,apiCreatorTenantDomain, "
                + configPropertyName + "; ";
        JSONObject result = APIUtil.executeQueryOnStreamProcessor(AlertMgtConstants.APIM_ALERT_CONFIG_APP, query);
        List<Map<String, String>> alertConfigProps = new ArrayList<>();
        if (result != null && result.get("records") != null) {
            JSONArray records = (JSONArray) result.get("records");
            for (Object record : records) {
                JSONArray config = (JSONArray) record;
                Map<String, String> properties = new HashMap<>();
                properties.put(AlertMgtConstants.API_NAME_KEY, (String) config.get(0));
                properties.put(AlertMgtConstants.API_VERSION_KEY, (String) config.get(1));
                properties.put(AlertMgtConstants.API_CREATOR_KEY, (String) config.get(2));
                properties.put(AlertMgtConstants.API_CREATOR_TENANT_DOMAIN_KEY, (String) config.get(3));
                properties.put(configPropertyName, String.valueOf(config.get(4)));
                alertConfigProps.add(properties);
            }
        }
        return alertConfigProps;
    }

    @Override
    public void removeAlertConfiguration(String userName, String alertName, Map<String, String> configProperties)
            throws APIManagementException {
        String apiName = configProperties.get(AlertMgtConstants.API_NAME_KEY);
        String apiVersion = configProperties.get(AlertMgtConstants.API_VERSION_KEY);
        String alertConfigDeleteQuery = buildAlertConfigDeleteQuery(apiName, apiVersion, userName, alertName);
        APIUtil.executeQueryOnStreamProcessor(AlertMgtConstants.APIM_ALERT_CONFIG_APP, alertConfigDeleteQuery);
    }

    private String buildAddConfigQuery(String userName, String apiName, String apiVersion, String alertType,
            String alertConfigValue) throws APIManagementException {

        int thresholdResponseTime = 0;
        int thresholdBackendTime = 0;
        String conditionQuery = "";

        if (AlertMgtConstants.ABNORMAL_RESPONSE_TIME_ALERT.equals(alertType)) {
            thresholdResponseTime = Integer.parseInt(alertConfigValue);
            conditionQuery = "set ApiCreatorAlertConfiguration.thresholdResponseTime = thresholdResponseTime " ;
        } else if (AlertMgtConstants.ABNORMAL_BACKEND_TIME_ALERT.equals(alertType)) {
            thresholdBackendTime = Integer.parseInt(alertConfigValue);
            conditionQuery = "set ApiCreatorAlertConfiguration.thresholdBackendTime = thresholdBackendTime " ;
        } else {
            throw new APIManagementException("Alert type does not support adding configuration");
        }

        String domainName = MultitenantUtils.getTenantDomain(userName);
        return "select '" + apiName + "' as apiName, '" + apiVersion + "' as apiVersion, '"
                + userName + "' as apiCreator, '" + domainName + "' as apiCreatorTenantDomain,"
                + thresholdResponseTime + "L as thresholdResponseTime,"
                + thresholdBackendTime + "L as thresholdBackendTime update or insert into ApiCreatorAlertConfiguration "
                + conditionQuery + "on ApiCreatorAlertConfiguration.apiName == apiName "
                + "and ApiCreatorAlertConfiguration.apiVersion == apiVersion " 
                + "and ApiCreatorAlertConfiguration.apiCreator == apiCreator " 
                + "and ApiCreatorAlertConfiguration.apiCreatorTenantDomain == apiCreatorTenantDomain";
    }

    private String buildAlertConfigDeleteQuery(String apiName, String apiVersion, String userName, String alertType) {
        String alertConfigKey = AlertMgtConstants.alertTypeConfigMap.get(alertType);
        String domainName = MultitenantUtils.getTenantDomain(userName);
        return "select '" + apiName + "' as apiName, '" + apiVersion + "' as apiVersion, '"
                + userName + "' as apiCreator, '" + domainName + "' as apiCreatorTenantDomain,"
                + "0L as " + alertConfigKey +" update ApiCreatorAlertConfiguration "
                + "set ApiCreatorAlertConfiguration." + alertConfigKey + " = " + alertConfigKey + " "
                + "on ApiCreatorAlertConfiguration.apiName == apiName "
                + "and ApiCreatorAlertConfiguration.apiVersion == apiVersion "
                + "and ApiCreatorAlertConfiguration.apiCreator == apiCreator";
    }
}
