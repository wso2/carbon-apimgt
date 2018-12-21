package org.wso2.carbon.apimgt.rest.api.store.utils.mappings;

import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIResultDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentResultDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ResultDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ResultListDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.Map;

public class SearchResultMappingUtil {


    /**
     *
     * Get API result representation for content search
     * @param api API
     * @return
     */
    public static APIResultDTO fromAPIToAPIResultDTO(API api) {
        APIResultDTO apiResultDTO = new APIResultDTO();
        apiResultDTO.setId(api.getUUID());
        APIIdentifier apiId = api.getId();
        apiResultDTO.setName(apiId.getApiName());
        apiResultDTO.setVersion(apiId.getVersion());
        apiResultDTO.setProvider(apiId.getProviderName());
        String context = api.getContextTemplate();
        if (context.endsWith("/" + RestApiConstants.API_VERSION_PARAM)) {
            context = context.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
        }
        apiResultDTO.setContext(context);
        apiResultDTO.setType(ResultDTO.TypeEnum.API);
        apiResultDTO.setDescription(api.getDescription());
        apiResultDTO.setStatus(api.getStatus());
        apiResultDTO.setThumbnailUri(api.getThumbnailUrl());
        return apiResultDTO;
    }

    /**
     *
     * @param document
     * @return
     */
    public static DocumentResultDTO fromDocumentationToDocumentResultDTO(Documentation document, API api) {
        DocumentResultDTO docResultDTO = new DocumentResultDTO();
        docResultDTO.setId(document.getId());
        docResultDTO.setName(document.getName());
        docResultDTO.setDocType(DocumentResultDTO.DocTypeEnum.valueOf(document.getType().toString()));
        docResultDTO.setType(ResultDTO.TypeEnum.DOC);
        docResultDTO.setSummary(document.getSummary());
        docResultDTO.setVisibility(DocumentResultDTO.VisibilityEnum.valueOf(document.getVisibility().toString()));
        docResultDTO.setSourceType(DocumentResultDTO.SourceTypeEnum.valueOf(document.getSourceType().toString()));
        docResultDTO.setOtherTypeName(document.getOtherTypeName());
        APIIdentifier apiId = api.getId();
        docResultDTO.setApiName(apiId.getApiName());
        docResultDTO.setApiVersion(apiId.getVersion());
        docResultDTO.setApiProvider(apiId.getProviderName());
        return docResultDTO;
    }

    /**
     * Sets pagination urls for a ResultListDTO object given pagination parameters and url parameters
     *
     * @param resultListDTO a ResultListDTO object
     * @param query      search condition
     * @param limit      max number of objects returned
     * @param offset     starting index
     * @param size       max offset
     */
    public static void setPaginationParams(ResultListDTO resultListDTO, String query, int offset, int limit, int size) {

        //acquiring pagination parameters and setting pagination urls
        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, size);
        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiUtil
                    .getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), query);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiUtil
                    .getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), query);
        }

        resultListDTO.setNext(paginatedNext);
        resultListDTO.setPrevious(paginatedPrevious);
    }

}
