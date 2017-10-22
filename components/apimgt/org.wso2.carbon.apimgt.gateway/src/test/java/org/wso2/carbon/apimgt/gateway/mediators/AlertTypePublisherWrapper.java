/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataPublisher;

public class AlertTypePublisherWrapper extends AlertTypesPublisher {

    private ApiMgtDAO apiMgtDAO;
    private static final Log log = LogFactory.getLog(AlertTypePublisherWrapper.class);
    private APIMgtUsageDataPublisher apiMgtUsageDataPublisher;

    @Override
    protected void initializeDataPublisher() {
        publisher =apiMgtUsageDataPublisher;
        log.info("inside initializeDataPublisher");
    }

    public AlertTypePublisherWrapper(ApiMgtDAO apiMgtDAO) {
        this.apiMgtDAO = apiMgtDAO;
    }

    public AlertTypePublisherWrapper(ApiMgtDAO apiMgtDAO, APIMgtUsageDataPublisher apiMgtUsageDataPublisher) {
        this.apiMgtDAO = apiMgtDAO;
        this.apiMgtUsageDataPublisher = apiMgtUsageDataPublisher;
    }

    @Override
    protected ApiMgtDAO getApiMgtdao() {
        return apiMgtDAO;
    }
}
