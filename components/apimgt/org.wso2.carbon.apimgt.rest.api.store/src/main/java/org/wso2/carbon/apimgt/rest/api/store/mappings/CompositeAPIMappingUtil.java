package org.wso2.carbon.apimgt.rest.api.store.mappings;

import org.wso2.carbon.apimgt.core.models.CompositeAPI;
import org.wso2.carbon.apimgt.rest.api.store.dto.CompositeAPIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.CompositeAPIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.CompositeAPIListDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CompositeAPIMappingUtil {

    /**
     * Converts {@link CompositeAPI} to a {@link CompositeAPIDTO}.
     *
     * @param api CompositeAPI
     * @return API DTO
     */
    public static CompositeAPIDTO toCompositeAPIDTO(CompositeAPI api) {
        CompositeAPIDTO compositeAPIDTO = new CompositeAPIDTO();
        compositeAPIDTO.setId(api.getId());
        compositeAPIDTO.setName(api.getName());
        compositeAPIDTO.setProvider(api.getProvider());
        compositeAPIDTO.setVersion(api.getVersion());
        compositeAPIDTO.setContext(api.getContext());
        compositeAPIDTO.setDescription(api.getDescription());
        compositeAPIDTO.setLabels(new ArrayList<>(api.getLabels()));
        compositeAPIDTO.setApplicationId(api.getApplicationId());
        compositeAPIDTO.hasOwnGateway(api.hasOwnGateway());
        return compositeAPIDTO;
    }

    /**
     * This method converts the CompositeAPI model object from the DTO object.
     *
     * @param apidto CompositeAPIDTO object with CompositeAPI data
     * @return CompositeAPI.Builder object
     */
    public static CompositeAPI.Builder toAPI(CompositeAPIDTO apidto) {
        return new CompositeAPI.Builder().id(apidto.getId()).
                provider(apidto.getProvider()).
                name(apidto.getName()).
                version(apidto.getVersion()).
                context(apidto.getContext()).
                description(apidto.getDescription()).
                labels(new ArrayList<>(apidto.getLabels())).
                hasOwnGateway(apidto.getHasOwnGateway()).
                transport(new HashSet<>(apidto.getTransport())).
                apiDefinition(apidto.getApiDefinition()).
                applicationId(apidto.getApplicationId());

    }

    /**
     * Converts API list to CompositeAPIListDTO list.
     *
     * @param apisResult List of APIs
     * @return CompositeAPIListDTO object
     */
    public static CompositeAPIListDTO toCompositeAPIListDTO(List<CompositeAPI> apisResult) {
        CompositeAPIListDTO apiListDTO = new CompositeAPIListDTO();
        apiListDTO.setCount(apisResult.size());
        // apiListDTO.setNext(next);
        // apiListDTO.setPrevious(previous);
        apiListDTO.setList(toCompositeAPIInfo(apisResult));
        return apiListDTO;
    }

    /**
     * Converts {@link CompositeAPI} List to an {@link CompositeAPIInfoDTO} List.
     *
     * @param apiSummaryList
     * @return
     */
    private static List<CompositeAPIInfoDTO> toCompositeAPIInfo(List<CompositeAPI> apiSummaryList) {
        List<CompositeAPIInfoDTO> apiInfoList = new ArrayList<>();
        for (CompositeAPI apiSummary : apiSummaryList) {
            CompositeAPIInfoDTO apiInfo = new CompositeAPIInfoDTO();
            apiInfo.setId(apiSummary.getId());
            apiInfo.setContext(apiSummary.getContext());
            apiInfo.setDescription(apiSummary.getDescription());
            apiInfo.setName(apiSummary.getName());
            apiInfo.setProvider(apiSummary.getProvider());
            apiInfo.setVersion(apiSummary.getVersion());
            apiInfo.setApplicationId(apiSummary.getApplicationId());
            apiInfoList.add(apiInfo);
        }
        return apiInfoList;
    }
}
