package org.wso2.carbon.graphql.api.devportal.service;

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.graphql.api.devportal.mapping.OperationMapping;
import org.wso2.carbon.graphql.api.devportal.modules.api.OperationDTO;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OperationService {


    public List<OperationDTO> getOperationDetails(String Id) throws  APIManagementException {
        String username = "wso2.anonymous.user";
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
        String type = apiConsumer.getApiTypeFromDAO(Id);
        OperationMapping operationMapping = new OperationMapping();
        List<OperationDTO> operationList = operationMapping.fromOperationDetailstoOperationDTO(apiConsumer.getURITemplateFromDAO(Id),type);
        return operationList;
    }
}
