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

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OperationService {


    public List<OperationDTO> getOperationDetails(List<ContextDTO> contextDTOList, String uuid) throws  APIManagementException {
        String username = "wso2.anonymous.user";
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
        String type = null;
        for(int i = 0 ;i< contextDTOList.size();i++){
            if (contextDTOList.get(i).getId().equals(uuid)){
                type = contextDTOList.get(i).getType();
            }
        }
        OperationMapping operationMapping = new OperationMapping();
        List<OperationDTO> operationList = operationMapping.fromOperationDetailstoOperationDTO(apiConsumer.getURITemplateFromDAO(uuid),type);
        return operationList;
    }
}
