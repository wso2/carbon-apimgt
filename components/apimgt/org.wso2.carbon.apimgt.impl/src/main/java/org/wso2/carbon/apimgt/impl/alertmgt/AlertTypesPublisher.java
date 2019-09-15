package org.wso2.carbon.apimgt.impl.alertmgt;
/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.sql.SQLException;

public class AlertTypesPublisher {

    private static final Log log = LogFactory.getLog(AlertTypesPublisher.class);
    protected boolean enabled;
    protected boolean skipEventReceiverConnection;
    private boolean isSubscriber = false;
    private boolean isPublisher = false;
    private boolean isAdmin = false;
    private ApiMgtDAO apiMgtDAO = null;

    public AlertTypesPublisher() {
        enabled = APIUtil.isAnalyticsEnabled();
        skipEventReceiverConnection = ServiceReferenceHolder.getInstance().getAPIManagerConfigurationService()
                .getAPIAnalyticsConfiguration().isSkipEventReceiverConnection();
        apiMgtDAO = ApiMgtDAO.getInstance();

    }

    public void saveAndPublishAlertTypesEvent(String checkedAlertList, String emailList, String userName, String agent,
            String checkedAlertListValues) throws APIManagementException {

        if (!enabled || skipEventReceiverConnection) {
            throw new APIManagementException("Data publisher is not enabled");
        }

        String conditionClause = "";
        //data persist in the database.
        apiMgtDAO.addAlertTypesConfigInfo(userName, emailList, checkedAlertList, agent);

        if ("publisher".equals(agent)) {
            conditionClause = "ApimAlertStakeholderInfo.isPublisher == isPublisher";
            isPublisher = true;
        } else if ("subscriber".equals(agent)) {
            conditionClause = "ApimAlertStakeholderInfo.isSubscriber == isSubscriber";
            isSubscriber = true;
        } else if ("admin-dashboard".equals(agent)) {
            conditionClause = "ApimAlertStakeholderInfo.isAdmin == isAdmin";
            isAdmin = true;
        }
        String appName = "APIM_ALERT_STAKEHOLDER";
        String query =
                "select '" + userName + "' as userId, '" + checkedAlertListValues + "' as alertTypes, '" + emailList
                        + "' as emails, " + isSubscriber + " as isSubscriber, " + isPublisher + " as isPublisher, "
                        + isAdmin + " as isAdmin update or insert into ApimAlertStakeholderInfo "
                        + "set ApimAlertStakeholderInfo.userId = userId, "
                        + "ApimAlertStakeholderInfo.alertTypes = alertTypes , "
                        + "ApimAlertStakeholderInfo.emails = emails ,"
                        + " ApimAlertStakeholderInfo.isSubscriber = isSubscriber, "
                        + "ApimAlertStakeholderInfo.isPublisher = isPublisher, "
                        + "ApimAlertStakeholderInfo.isAdmin = isAdmin on "
                        + "ApimAlertStakeholderInfo.userId == userId and " + conditionClause;
        APIUtil.executeQueryOnStreamProcessor(appName, query);

    }

    /**
     * This method will delete all the data relating to the alert subscription by given user Name.
     *
     * @param userName logged in users name.
     */
    public void unSubscribe(String userName, String agent) throws APIManagementException {

        if (!enabled || skipEventReceiverConnection) {
            throw new APIManagementException("Data publisher is not enabled");
        }

        String conditionClause = "";
        //data persist in the database.
        apiMgtDAO.unSubscribeAlerts(userName, agent);
        //set DTO

        if ("publisher".equals(agent)) {
            conditionClause = "ApimAlertStakeholderInfo.isPublisher == true";
            isPublisher = true;
        } else if ("subscriber".equals(agent)) {
            conditionClause = "ApimAlertStakeholderInfo.isSubscriber == true";
            isSubscriber = true;
        } else if ("admin-dashboard".equals(agent)) {
            conditionClause = "ApimAlertStakeholderInfo.isAdmin == true";
            isAdmin = true;
        }
        String appName = "APIM_ALERT_STAKEHOLDER";
        String query = "delete ApimAlertStakeholderInfo  on ApimAlertStakeholderInfo.userId == '" + userName + "' and "
                + conditionClause;
        APIUtil.executeQueryOnStreamProcessor(appName, query);

    }

}
