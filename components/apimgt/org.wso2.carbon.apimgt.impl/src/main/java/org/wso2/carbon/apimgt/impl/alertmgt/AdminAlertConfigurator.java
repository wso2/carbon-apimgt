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
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.util.List;
import java.util.Map;

public class AdminAlertConfigurator extends AlertConfigurator {

    private String agent;
    private static final Log log = LogFactory.getLog(AdminAlertConfigurator.class);
    private ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

    public AdminAlertConfigurator() {
        this.agent = AlertMgtConstants.ADMIN_DASHBOARD_AGENT;
    }

    /**
     * Subscribe for admin alerts
     *
     * @param userName          : The username of the user, who is subscribing.
     * @param emailsList        : The list of emails which needs to be subscribed.
     * @param alertTypeDTOList: The list of Alert types which needs to be subscribed.
     * @throws APIManagementException
     */
    @Override public void subscribe(String userName, List<String> emailsList, List<AlertTypeDTO> alertTypeDTOList)
            throws APIManagementException {

        String emails = StringUtils.join(emailsList, ",");
        Map<String, String> alertTypesMap = AlertMgtUtils.alertTypesToMap(alertTypeDTOList);

        if (log.isDebugEnabled()) {
            log.debug(
                    "Subscribing user: " + userName + " for alert types: " + alertTypesMap.get("ids") + " with emails: "
                            + emails);
        }

        String query =
                "select '" + userName + "' as userId, '" + alertTypesMap.get("names") + "' as alertTypes, '" + emails
                        + "' as emails, false as isSubscriber, false as isPublisher, "
                        + "true as isAdmin update or insert into ApimAlertStakeholderInfo "
                        + "set ApimAlertStakeholderInfo.userId = userId, "
                        + "ApimAlertStakeholderInfo.alertTypes = alertTypes , "
                        + "ApimAlertStakeholderInfo.emails = emails , "
                        + "ApimAlertStakeholderInfo.isSubscriber = isSubscriber, "
                        + "ApimAlertStakeholderInfo.isPublisher = isPublisher, "
                        + "ApimAlertStakeholderInfo.isAdmin = isAdmin on "
                        + "ApimAlertStakeholderInfo.userId == userId and "
                        + "ApimAlertStakeholderInfo.isPublisher == isPublisher";
        APIUtil.executeQueryOnStreamProcessor(AlertMgtConstants.APIM_STAKEHOLDER_ALERT_APP, query);
        //Persist the alert subscription in database
        apiMgtDAO.addAlertTypesConfigInfo(userName, emails, alertTypesMap.get("ids"),
                AlertMgtConstants.ADMIN_DASHBOARD_AGENT);
    }

    /**
     * Unsubscribe from all admin alerts
     *
     * @param userName : The name of the user who needs to be unsubscribed.
     * @throws APIManagementException
     */
    @Override public void unsubscribe(String userName) throws APIManagementException {

        if (log.isDebugEnabled()) {
            log.debug("Unsubscribing user: " + userName + " for all alert types");
        }

        //Removing the existing alert subscription information from the database
        apiMgtDAO.unSubscribeAlerts(userName, AlertMgtConstants.ADMIN_DASHBOARD_AGENT);
        String query = "delete ApimAlertStakeholderInfo on ApimAlertStakeholderInfo.userId == '" + userName + "' and "
                + "ApimAlertStakeholderInfo.isAdmin == true";
        APIUtil.executeQueryOnStreamProcessor(AlertMgtConstants.APIM_STAKEHOLDER_ALERT_APP, query);
    }

    @Override public void addAlertConfiguration(String userName, String alertName, Map<String, String> configProperties)
            throws APIManagementException {

    }

    @Override public List<Map<String, String>> getAlertConfiguration(String userName, String alertName)
            throws APIManagementException {
        return null;
    }

    @Override public void removeAlertConfiguration(String userName, String alertName,
            Map<String, String> configProperties) throws APIManagementException {
    }

    public List<AlertTypeDTO> getSupportedAlertTypes() throws APIManagementException {
        return super.getSupportedAlertTypes(this.agent);
    }

    public List<Integer> getSubscribedAlerts(String userName) throws APIManagementException {
        return super.getSubscribedAlerts(userName, this.agent);
    }

    public List<String> getSubscribedEmailAddresses(String userName) throws APIManagementException {
        return super.getSubscribedEmailAddresses(userName, this.agent);
    }

}