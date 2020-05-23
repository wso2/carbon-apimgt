package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeListDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for mapping Scope Objects into REST API Scope related DTOs.
 */
public class SharedScopeMappingUtil {

    private SharedScopeMappingUtil() {

        throw new IllegalStateException("SharedScope Mapping Utility class");
    }

    /**
     * Converts Scope object into ScopeDTO object.
     *
     * @param scope Scope object
     * @return ScopeDTO object
     */
    public static ScopeDTO fromScopeToDTO(Scope scope) {

        ScopeDTO scopeDTO = new ScopeDTO();
        scopeDTO.setName(scope.getKey());
        scopeDTO.setDisplayName(scope.getName());
        scopeDTO.setDescription(scope.getDescription());
        scopeDTO.setId(scope.getId());
        String roles = scope.getRoles();
        if (StringUtils.isEmpty(roles)) {
            scopeDTO.setBindings(Collections.emptyList());
        } else {
            scopeDTO.setBindings(Arrays.asList((roles).split(",")));
        }
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
        scope.setName(scopeDTO.getDisplayName());
        if (scopeDTO.getBindings() != null) {
            scope.setRoles(String.join(",", scopeDTO.getBindings()));
        }
        return scope;
    }

    /**
     * Converts a list of Scope objects into a SharedScopeListDTO.
     *
     * @param scopeList List of Scope objects
     * @param offset    max number of objects returned
     * @param limit     starting index
     * @return SharedScopeListDTO object
     */
    public static ScopeListDTO fromScopeListToDTO(List<Scope> scopeList, int offset, int limit) {

        ScopeListDTO sharedScopeListDTO = new ScopeListDTO();
        List<ScopeDTO> scopeDTOList = sharedScopeListDTO.getList();
        if (scopeList == null) {
            scopeList = new ArrayList<>();
            sharedScopeListDTO.setList(scopeDTOList);
        }
        //identifying the proper start and end indexes
        int size = scopeList.size();
        int start = offset < size && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = Math.min(offset + limit - 1, size - 1);
        for (int i = start; i <= end; i++) {
            scopeDTOList.add(fromScopeToDTO(scopeList.get(i)));
        }
        sharedScopeListDTO.setCount(scopeDTOList.size());
        return sharedScopeListDTO;
    }

    /**
     * Sets pagination urls for a shared ScopeListDTO object given pagination parameters and url parameters.
     *
     * @param sharedScopeListDTO a ScopeListDTO object
     * @param limit              max number of objects returned
     * @param offset             starting index
     * @param size               max offset
     */
    public static void setPaginationParams(ScopeListDTO sharedScopeListDTO, int limit, int offset, int size) {

        String paginatedPrevious = "";
        String paginatedNext = "";

        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, size);

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiUtil
                    .getScopesPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT));
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiUtil
                    .getScopesPaginatedURL(
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT));
        }
        PaginationDTO paginationDTO = CommonMappingUtil
                .getPaginationDTO(limit, offset, size, paginatedNext, paginatedPrevious);
        sharedScopeListDTO.setPagination(paginationDTO);
    }
}
