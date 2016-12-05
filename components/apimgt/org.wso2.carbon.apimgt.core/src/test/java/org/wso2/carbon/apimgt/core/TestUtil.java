/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.apimgt.core;

import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.impl.DAOFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Application;

public class TestUtil {

    public static Application addTestApplication() throws APIMgtDAOException {
        ApplicationDAO applicationDAO = DAOFactory.getApplicationDAO();
        Application application = SampleTestObjectCreator.createDefaultApplication();
        applicationDAO.addApplication(application);
        return application;
    }

    public static Application addCustomApplication(String applicationName, String owner) throws APIMgtDAOException {
        ApplicationDAO applicationDAO = DAOFactory.getApplicationDAO();
        Application application = SampleTestObjectCreator.createCustomApplication(applicationName, owner);
        applicationDAO.addApplication(application);
        return application;
    }

    public static API addTestAPI() throws APIManagementException {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API api = SampleTestObjectCreator.createDefaultAPI().build();
        apiDAO.addAPI(api);
        return api;
    }

    public static API addCustomAPI(String name, String version, String context) throws APIManagementException {
        ApiDAO apiDAO = DAOFactory.getApiDAO();
        API api = SampleTestObjectCreator.createCustomAPI(name, version, context).build();
        apiDAO.addAPI(api);
        return api;
    }

    /**
     * Creates a summary API object which has only API ID, Name, Provider, Version and Context
     *
     * @param api Complete API Object
     * @return Summary API object
     * @throws APIManagementException
     */
    public static API createSummaryAPI(API api) throws APIManagementException {
        API.APIBuilder summaryApiBuilder = new API.APIBuilder(api.getProvider(), api.getName(), api.getVersion());
        summaryApiBuilder.id(api.getId());
        summaryApiBuilder.context(api.getContext());
        return summaryApiBuilder.build();
    }

    /**
     * Create a summary Applocation only with Application ID, Name, Callback URL, Application Owner and Status
     *
     * @param app Complete Application object
     * @return Summary Application object
     */
    public static Application createSummaryApplication(Application app){
        Application summaryApp = new Application(app.getName(), app.getCreatedUser());
        summaryApp.setId(app.getId());
        summaryApp.setCallbackUrl(app.getCallbackUrl());
        summaryApp.setStatus(app.getStatus());
        return summaryApp;
    }
}
