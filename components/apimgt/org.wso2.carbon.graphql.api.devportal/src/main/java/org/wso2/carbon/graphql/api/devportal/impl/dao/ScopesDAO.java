package org.wso2.carbon.graphql.api.devportal.impl.dao;

import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.graphql.api.devportal.modules.api.ScopesDTO;
import org.wso2.carbon.apimgt.api.model.Scope;
import java.util.*;


public class ScopesDAO {

    public List<ScopesDTO> getScopesData(String Id) throws APIManagementException{


        String username = "wso2.anonymous.user";
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
        List<Scope> scopeList = apiConsumer.getScopeDataDromDAO(Id);//new ArrayList<>(scopes);

        List<ScopesDTO> scopeData = new ArrayList<ScopesDTO>();

        for (int i=0 ; i<scopeList.size();i++){
            String key = scopeList.get(i).getKey();
            String name = scopeList.get(i).getName();
            String role = scopeList.get(i).getRoles();
            String description = scopeList.get(i).getDescription();

            scopeData.add(new ScopesDTO(key,name,role,description));
        }

        return  scopeData;
    }
}
