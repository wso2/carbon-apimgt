/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.wso2.carbon.apimgt.impl.recommendationmgt;

import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;

import java.io.IOException;

public interface RecommenderEventPublisher extends Runnable {

    void run();

    void publishAPIDetails(API api, String tenantDomain) throws IOException;

    void publishApplicationDetails(Application application, String userId, int applicationId);

    void publishDeletedApplication(int appId);

    void publishClickedApi(ApiTypeWrapper api, String userName);

    void publishSearchQueries(String query, String username);
}
