package org.wso2.carbon.graphql.api.devportal.service;

import graphql.schema.DataFetchingFieldSelectionSet;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.graphql.api.devportal.mapping.OperationMapping;
import org.wso2.carbon.graphql.api.devportal.modules.api.ContextDTO;
import org.wso2.carbon.graphql.api.devportal.modules.api.OperationDTO;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.graphql.api.devportal.security.AuthenticationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class OperationService {



    public List<OperationDTO> getOperationDetails(Map<String, ContextDTO> stringContextDTOMap, String uuid) throws  APIManagementException {
        String loggedInUserName= AuthenticationContext.getLoggedInUserName();
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(loggedInUserName);
        String type = stringContextDTOMap.get(uuid).getType();
        OperationMapping operationMapping = new OperationMapping();
        List<OperationDTO> operationList = operationMapping.fromOperationDetailstoOperationDTO(apiConsumer.getURITemplateFromDAO(uuid),type);
        return operationList;
    }
}
