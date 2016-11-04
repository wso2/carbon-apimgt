/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.mediators;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.mediators.APIMgtCommonExecutionPublisher;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.usage.publisher.dto.AlertTypeDTO;

import java.sql.SQLException;

/**
 * This is class will be called from jaggery layer and has methods to persist and publish alert types data to the its
 * stream.
 */
public class AlertTypesPublisher extends APIMgtCommonExecutionPublisher {

    public AlertTypesPublisher() {
        super();
    }

    /**
     *
     * @param checkedAlertList Comma separated checked list ids.
     * @param emailList Comma separated email list.
     * @param userName user name
     * @param agent publisher "publisher" store "store" admin dashboard "admin-dashboard".
     * @param checkedAlertListValues alert type name lists.
     * @throws APIManagementException
     */
    public void saveAndPublishAlertTypesEvent(String checkedAlertList, String emailList, String userName, String agent,
            String checkedAlertListValues) throws APIManagementException {

        try {

            if (!enabled || skipEventReceiverConnection) {
                throw new APIManagementException("Data publisher is not enabled");
            }

            if (publisher == null) {
                this.initializeDataPublisher();
            }

            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
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
            }else if("admin-dashboard".equals(agent)){
                alertTypeDTO.setSubscriber(true);
                alertTypeDTO.setPublisher(true);
            }
            publisher.publishEvent(alertTypeDTO);

        } catch (SQLException e) {
            handleException("Error while saving alert types", e);
        }

    }

    /**
     * This method will delete all the data relating to the alert subscription by given user Name.
     * @param userName logged in users name.
     */
    public void unSubscribe(String userName,String agent) throws APIManagementException {

        try {

            if (!enabled || skipEventReceiverConnection) {
                throw new APIManagementException("Data publisher is not enabled");
            }

            if (publisher == null) {
                this.initializeDataPublisher();
            }

            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();
            //data persist in the database.
            apiMgtDAO.unSubscribeAlerts(userName,agent);
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
            }else if("admin-dashboard".equals(agent)){
                alertTypeDTO.setSubscriber(true);
                alertTypeDTO.setPublisher(true);
            }
            publisher.publishEvent(alertTypeDTO);

        } catch (SQLException e) {
            handleException("Error while saving alert types", e);
        }

    }

    private void handleException(String msg, Throwable t) throws APIManagementException {
        log.error(msg, t);
        throw new APIManagementException(msg, t);
    }

}
