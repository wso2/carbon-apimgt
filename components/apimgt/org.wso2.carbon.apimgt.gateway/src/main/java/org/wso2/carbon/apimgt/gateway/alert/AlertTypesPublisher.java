package org.wso2.carbon.apimgt.gateway.alert;
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
import org.wso2.carbon.apimgt.impl.APIManagerAnalyticsConfiguration;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataPublisher;
import org.wso2.carbon.apimgt.usage.publisher.DataPublisherUtil;
import org.wso2.carbon.apimgt.usage.publisher.dto.AlertTypeDTO;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;

import java.sql.SQLException;

public class AlertTypesPublisher {
    private static final Log log = LogFactory.getLog(AlertTypesPublisher.class);
    protected boolean enabled;
    protected volatile APIMgtUsageDataPublisher publisher;
    protected boolean skipEventReceiverConnection;

    public AlertTypesPublisher() {
        enabled = APIUtil.isAnalyticsEnabled();
        skipEventReceiverConnection = DataPublisherUtil.getApiManagerAnalyticsConfiguration().
                isSkipEventReceiverConnection();
    }

    protected APIManagerAnalyticsConfiguration getApiManagerAnalyticsConfiguration() {
        return DataPublisherUtil.getApiManagerAnalyticsConfiguration();
    }

    public void saveAndPublishAlertTypesEvent(String checkedAlertList, String emailList, String userName, String agent,
            String checkedAlertListValues) throws APIManagementException {

        try {
            if (!enabled || skipEventReceiverConnection) {
                throw new APIManagementException("Data publisher is not enabled");
            }

            if (publisher == null) {
                this.initializeDataPublisher();
            }

            ApiMgtDAO apiMgtDAO = getApiMgtDao();
            //data persist in the database.
            apiMgtDAO.addAlertTypesConfigInfo(userName, emailList, checkedAlertList, agent);
            //set DTO
            AlertTypeDTO alertTypeDTO = new AlertTypeDTO();
            alertTypeDTO.setAlertTypes(checkedAlertListValues);
            alertTypeDTO.setEmails(emailList);
            alertTypeDTO.setUserName(userName);
            if ("publisher".equals(agent)) {
                alertTypeDTO.setPublisher(true);
                alertTypeDTO.setSubscriber(false);
            } else if ("subscriber".equals(agent)) {
                alertTypeDTO.setSubscriber(true);
                alertTypeDTO.setPublisher(false);
            } else if ("admin-dashboard".equals(agent)) {
                alertTypeDTO.setSubscriber(true);
                alertTypeDTO.setPublisher(true);
            }
            publisher.publishEvent(alertTypeDTO);

        } catch (SQLException e) {
            log.error("Error while saving alert types", e);
        }

    }

    protected ApiMgtDAO getApiMgtDao() {
        return ApiMgtDAO.getInstance();
    }

    protected void initializeDataPublisher() {

        if (!enabled || skipEventReceiverConnection) {
            return;
        }
        if (publisher == null) {
            synchronized (this) {
                if (publisher == null) {
                    String publisherClass = getApiManagerAnalyticsConfiguration().getPublisherClass();
                    try {
                        log.debug("Instantiating Data Publisher");
                        PrivilegedCarbonContext.startTenantFlow();
                        PrivilegedCarbonContext.getThreadLocalCarbonContext().
                                setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
                        APIMgtUsageDataPublisher tempPublisher = (APIMgtUsageDataPublisher) APIUtil
                                .getClassForName(publisherClass).newInstance();
                        tempPublisher.init();
                        publisher = tempPublisher;
                    } catch (ClassNotFoundException e) {
                        log.error("Class not found " + publisherClass, e);
                    } catch (InstantiationException e) {
                        log.error("Error instantiating " + publisherClass, e);
                    } catch (IllegalAccessException e) {
                        log.error("Illegal access to " + publisherClass, e);
                    } finally {
                        PrivilegedCarbonContext.endTenantFlow();
                    }
                }
            }
        }
    }

    /**
     * This method will delete all the data relating to the alert subscription by given user Name.
     *
     * @param userName logged in users name.
     */
    public void unSubscribe(String userName, String agent) throws APIManagementException {

        try {

            if (!enabled || skipEventReceiverConnection) {
                throw new APIManagementException("Data publisher is not enabled");
            }

            if (publisher == null) {
                this.initializeDataPublisher();
            }

            ApiMgtDAO apiMgtDAO = getApiMgtDao();
            //data persist in the database.
            apiMgtDAO.unSubscribeAlerts(userName, agent);
            //set DTO
            AlertTypeDTO alertTypeDTO = new AlertTypeDTO();
            alertTypeDTO.setAlertTypes("");
            alertTypeDTO.setEmails("");
            alertTypeDTO.setUserName(userName);
            if ("publisher".equals(agent)) {
                alertTypeDTO.setPublisher(true);
                alertTypeDTO.setSubscriber(false);
            } else if ("subscriber".equals(agent)) {
                alertTypeDTO.setSubscriber(true);
                alertTypeDTO.setPublisher(false);
            } else if ("admin-dashboard".equals(agent)) {
                alertTypeDTO.setSubscriber(true);
                alertTypeDTO.setPublisher(true);
            }
            publisher.publishEvent(alertTypeDTO);

        } catch (SQLException e) {
            log.error("Error while saving alert types", e);
        }

    }
}
