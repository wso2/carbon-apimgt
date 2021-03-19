package org.wso2.carbon.graphql.api.devportal.service;

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.graphql.api.devportal.mapping.ScopesMapping;
import org.wso2.carbon.graphql.api.devportal.modules.api.ScopesDTO;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.graphql.api.devportal.security.AuthenticationContext;

import java.util.*;


public class ScopesService {

    public List<ScopesDTO> getScopesDetails(String uuid) throws APIManagementException{


        String loggedInUserName= AuthenticationContext.getLoggedInUserName();

        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(loggedInUserName);
        List<Scope> scopeList = apiConsumer.getScopeDataDromDAO(uuid);//new ArrayList<>(scopes);

        ScopesMapping scopesMapping = new ScopesMapping();
        List<ScopesDTO> scopeData = scopesMapping.fromScopeToScopeDTO(scopeList);
        return  scopeData;
    }
}
