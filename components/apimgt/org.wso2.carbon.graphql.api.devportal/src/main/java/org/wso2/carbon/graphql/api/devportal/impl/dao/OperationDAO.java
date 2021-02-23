package org.wso2.carbon.graphql.api.devportal.impl.dao;

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.graphql.api.devportal.modules.api.OperationDTO;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OperationDAO {


    public List<OperationDTO> getOperationDetailsFromDAO(String Id) throws  APIManagementException {
        String username = "wso2.anonymous.user";
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
        String type = apiConsumer.getApiTypeFromDAO(Id);

        List<OperationDTO> operationList = new ArrayList<>();
        if (APIConstants.APITransportType.GRAPHQL.toString().equals(type)) {
            operationList = new ArrayList<>();
            Set<URITemplate> uriTemplates = apiConsumer.getURITemplateFromDAO(Id);
            for (URITemplate template : uriTemplates) {
                String target = template.getUriTemplate();
                String verb = template.getHTTPVerb();
                operationList.add(new OperationDTO(target,verb));
            }

        }
        return operationList;
    }
}
