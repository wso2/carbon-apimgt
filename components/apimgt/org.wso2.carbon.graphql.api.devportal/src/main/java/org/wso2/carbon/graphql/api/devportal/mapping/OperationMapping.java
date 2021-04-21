package org.wso2.carbon.graphql.api.devportal.mapping;

import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.graphql.api.devportal.modules.api.OperationDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OperationMapping {

    public List<OperationDTO> fromOperationDetailstoOperationDTO(Set<URITemplate> uriTemplates, String type){
        List<OperationDTO> operationList = new ArrayList<>();
        if (APIConstants.APITransportType.GRAPHQL.toString().equals(type)) {
            operationList = new ArrayList<>();
            for (URITemplate template : uriTemplates) {
                String target = template.getUriTemplate();
                String verb = template.getHTTPVerb();
                operationList.add(new OperationDTO(target,verb));
            }

        }
        return operationList;
    }
}
