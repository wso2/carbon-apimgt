package org.wso2.carbon.apimgt.impl.restapi.publisher;


import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.model.APIRevision;

import java.util.ArrayList;
import java.util.List;

public class ApiProductsApiServiceImplUtils {

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
