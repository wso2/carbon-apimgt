package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GlobalScopeListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeBindingsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeDTO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class is responsible for mapping Scope Objects into REST API Scope related DTOs
 */
public class GlobalScopeMappingUtil {

    /**
     * Converts Scope object into ScopeDTO object.
     *
     * @param scope Scope object
     * @return ScopeDTO object
     */
    public static ScopeDTO fromScopeToDTO(Scope scope) {

        ScopeDTO scopeDTO = new ScopeDTO();
        scopeDTO.setName(scope.getKey());
        scopeDTO.setDescription(scope.getDescription());
        scopeDTO.setId(scope.getId());
        ScopeBindingsDTO bindingsDTO = new ScopeBindingsDTO();
        String roles = scope.getRoles();
        if (StringUtils.isEmpty(roles)) {
            bindingsDTO.setValues(Collections.emptyList());
        } else {
            bindingsDTO.setValues(Arrays.asList((roles).split(",")));
        }
        scopeDTO.setBindings(bindingsDTO);
        return scopeDTO;
    }

    /**
     * Converts ScopeDTO object into Scope object.
     *
     * @param scopeDTO ScopeDTO object
     * @return Scope object
     */
    public static Scope fromDTOToScope(ScopeDTO scopeDTO) {

        Scope scope = new Scope();
        scope.setId(scopeDTO.getId());
        scope.setDescription(scopeDTO.getDescription());
        scope.setKey(scopeDTO.getName());
        scope.setName(scopeDTO.getName());
        ScopeBindingsDTO scopeBindingsDTO = scopeDTO.getBindings();
        if (scopeBindingsDTO != null && scopeBindingsDTO.getValues() != null) {
            scope.setRoles(String.join(",", scopeBindingsDTO.getValues()));
        }
        return scope;
    }

    /**
     * Converts a list of Scope objects into a GlobalScopeListDTO.
     *
     * @param scopeList List of Scope objects
     * @return GlobalScopeListDTO object
     */
    public static GlobalScopeListDTO fromScopeListToDTO(List<Scope> scopeList) {

        GlobalScopeListDTO globalScopeListDTO = new GlobalScopeListDTO();
        List<ScopeDTO> scopeDTOList = globalScopeListDTO.getList();
        if (scopeList == null) {
            scopeList = new ArrayList<>();
        }
        for (Scope scope : scopeList) {
            scopeDTOList.add(fromScopeToDTO(scope));
        }
        globalScopeListDTO.setList(scopeDTOList);
        globalScopeListDTO.setCount(scopeDTOList.size());
        return globalScopeListDTO;
    }

}
