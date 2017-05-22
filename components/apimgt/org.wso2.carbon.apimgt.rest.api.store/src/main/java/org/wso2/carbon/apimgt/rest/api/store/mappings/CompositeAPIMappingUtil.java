package org.wso2.carbon.apimgt.rest.api.store.mappings;

import org.wso2.carbon.apimgt.core.dao.ApiType;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.rest.api.store.dto.CompositeAPIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.CompositeAPIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.CompositeAPIListDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CompositeAPIMappingUtil {

    /**
     * Converts {@link API} to a {@link CompositeAPIDTO}.
     *
     * @param api API
     * @return API DTO
     */
    public static CompositeAPIDTO toCompositeAPIDTO(API api) {
        CompositeAPIDTO compositeAPIDTO = new CompositeAPIDTO();
        compositeAPIDTO.setId(api.getId());
        compositeAPIDTO.setName(api.getName());
        compositeAPIDTO.setProvider(api.getProvider());
        compositeAPIDTO.setVersion(api.getVersion());
        compositeAPIDTO.setContext(api.getContext());
        compositeAPIDTO.setDescription(api.getDescription());
        compositeAPIDTO.setLabels(new ArrayList<>(api.getLabels()));
        return compositeAPIDTO;
    }

    /**
     * This method converts the API model object from the DTO object.
     *
     * @param apidto CompositeAPIDTO object with API data
     * @return APIBuilder object
     */
    public static API.APIBuilder toAPI(CompositeAPIDTO apidto) {
        return new API.APIBuilder(apidto.getProvider(), apidto.getName(), apidto.getVersion()).
                id(apidto.getId()).
                context(apidto.getContext()).
                description(apidto.getDescription()).
                labels(new HashSet<>(apidto.getLabels())).
                transport(new HashSet<>(apidto.getTransport())).
                apiType(ApiType.COMPOSITE).
                apiDefinition(apidto.getApiDefinition());
    }

    /**
     * Converts API list to CompositeAPIListDTO list.
     *
     * @param apisResult List of APIs
     * @return CompositeAPIListDTO object
     */
    public static CompositeAPIListDTO toCompositeAPIListDTO(List<API> apisResult) {
        CompositeAPIListDTO apiListDTO = new CompositeAPIListDTO();
        apiListDTO.setCount(apisResult.size());
        // apiListDTO.setNext(next);
        // apiListDTO.setPrevious(previous);
        apiListDTO.setList(toCompositeAPIInfo(apisResult));
        return apiListDTO;
    }

    /**
     * Converts {@link API} List to an {@link CompositeAPIInfoDTO} List.
     *
     * @param apiSummaryList
     * @return
     */
    private static List<CompositeAPIInfoDTO> toCompositeAPIInfo(List<API> apiSummaryList) {
        List<CompositeAPIInfoDTO> apiInfoList = new ArrayList<>();
        for (API apiSummary : apiSummaryList) {
            CompositeAPIInfoDTO apiInfo = new CompositeAPIInfoDTO();
            apiInfo.setId(apiSummary.getId());
            apiInfo.setContext(apiSummary.getContext());
            apiInfo.setDescription(apiSummary.getDescription());
            apiInfo.setName(apiSummary.getName());
            apiInfo.setProvider(apiSummary.getProvider());
            apiInfo.setVersion(apiSummary.getVersion());
            apiInfoList.add(apiInfo);
        }
        return apiInfoList;
    }
}
