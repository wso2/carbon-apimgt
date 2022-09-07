/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.restapi.publisher;


import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.model.APIRevision;

import java.util.ArrayList;
import java.util.List;

public class ApiProductsApiServiceImplUtils {

    private ApiProductsApiServiceImplUtils() {
    }

    /**
     * @param query     Revision filter query
     * @param revisions List of revisions
     * @return List of filtered revisions
     */
    public static List<APIRevision> getAPIRevisionListDTO(String query, List<APIRevision> revisions) {
        if (StringUtils.equalsIgnoreCase("deployed:true", query)) {
            List<APIRevision> apiDeployedRevisions = new ArrayList<>();
            for (APIRevision apiRevision : revisions) {
                if (!apiRevision.getApiRevisionDeploymentList().isEmpty()) {
                    apiDeployedRevisions.add(apiRevision);
                }
            }
            return apiDeployedRevisions;
        } else if (StringUtils.equalsIgnoreCase("deployed:false", query)) {
            List<APIRevision> apiProductNotDeployedRevisions = new ArrayList<>();
            for (APIRevision apiRevision : revisions) {
                if (apiRevision.getApiRevisionDeploymentList().isEmpty()) {
                    apiProductNotDeployedRevisions.add(apiRevision);
                }
            }
            return apiProductNotDeployedRevisions;
        } else {
            return revisions;
        }
    }
}
