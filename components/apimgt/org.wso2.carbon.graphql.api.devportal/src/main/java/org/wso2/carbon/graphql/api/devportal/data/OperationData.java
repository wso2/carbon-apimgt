package org.wso2.carbon.graphql.api.devportal.data;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.governance.api.exception.GovernanceException;
import org.wso2.carbon.graphql.api.devportal.ArtifactData;
import org.wso2.carbon.graphql.api.devportal.modules.OperationDTO;
import org.wso2.carbon.graphql.api.devportal.RegistryData;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OperationData {


    public List<OperationDTO> getOperationData(String Id) throws GovernanceException, APIManagementException {



        APIDTOData apidtoData = new APIDTOData();

        APIIdentifier apiIdentifier1 = ApiMgtDAO.getInstance().getAPIIdentifierFromUUID(Id);

        String type = apidtoData.getApiType(Id);

        List<OperationDTO> operationList = null;
        if (APIConstants.APITransportType.GRAPHQL.toString().equals(type)) {
            operationList = new ArrayList<>();
            Set<URITemplate> uriTemplates = ApiMgtDAO.getInstance().getURITemplatesOfAPI(apiIdentifier1);
            for (URITemplate template : uriTemplates) {
                String target = template.getUriTemplate();
                String verb = template.getHTTPVerb();
                operationList.add(new OperationDTO(target,verb));
            }

        }
        return operationList;
    }
}
