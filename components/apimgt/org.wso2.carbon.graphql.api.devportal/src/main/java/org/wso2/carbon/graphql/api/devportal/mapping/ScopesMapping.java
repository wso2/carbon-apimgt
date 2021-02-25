package org.wso2.carbon.graphql.api.devportal.mapping;

import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.graphql.api.devportal.modules.api.ScopesDTO;

import java.util.ArrayList;
import java.util.List;

public class ScopesMapping {

    public List<ScopesDTO> fromScopeToScopeDTO( List<Scope> scopeList){
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
