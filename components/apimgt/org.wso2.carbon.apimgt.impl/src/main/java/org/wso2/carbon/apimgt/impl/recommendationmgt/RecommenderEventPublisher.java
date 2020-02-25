/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.impl.recommendationmgt;

import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Application;

import java.io.IOException;

public interface RecommenderEventPublisher extends Runnable {

    void run();

    void publishAPIdetails(API api, String tenantDomain) throws IOException;

    void publishNewApplication(Application application, String userId, int applicationId);

    void publishUpdatedApplication(Application application) throws IOException;

    void publishedDeletedApplication(int appId);

    void publishClickedApi(ApiTypeWrapper api, String userName);

    void publishSearchQueries(String query, String username);
}
