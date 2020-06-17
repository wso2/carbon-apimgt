package org.wso2.carbon.apimgt.rest.api.store.v1.mappings;

import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class is responsible for mapping APIM core Query Analysis related objects into REST API documentation
 * related DTOs
 */
public class GraphqlQueryAnalysisMappingUtil {

    /**
     * Converts a GraphqlComplexityInfo object into a DTO object
     *
     * @param graphqlComplexityInfo GraphqlComplexityInfo object
     * @return a new GraphQLQueryComplexityInfoDTO object corresponding to given GraphqlComplexityInfo object
     */
    public static GraphQLQueryComplexityInfoDTO fromGraphqlComplexityInfotoDTO(
            GraphqlComplexityInfo graphqlComplexityInfo) {
        GraphQLQueryComplexityInfoDTO graphQLQueryComplexityInfoDTO = new GraphQLQueryComplexityInfoDTO();
        List<GraphQLCustomComplexityInfoDTO> graphQLCustomComplexityInfoDTOList = new ArrayList<GraphQLCustomComplexityInfoDTO>();
        for (CustomComplexityDetails customComplexityDetails : graphqlComplexityInfo.getList()) {
            GraphQLCustomComplexityInfoDTO graphQLCustomComplexityInfoDTO = new GraphQLCustomComplexityInfoDTO();
            graphQLCustomComplexityInfoDTO.setType(customComplexityDetails.getType());
            graphQLCustomComplexityInfoDTO.setField(customComplexityDetails.getField());
            graphQLCustomComplexityInfoDTO.setComplexityValue(customComplexityDetails.getComplexityValue());
            graphQLCustomComplexityInfoDTOList.add(graphQLCustomComplexityInfoDTO);
        }
        graphQLQueryComplexityInfoDTO.setList(graphQLCustomComplexityInfoDTOList);
        return graphQLQueryComplexityInfoDTO;
    }

    /**
     * Converts a list of GraphqlSchemaType objects into a DTO object
     *
     * @param typeList List<GraphqlSchemaType>
     * @return a new GraphQLSchemaTypeListDTO object corresponding to given list of GraphqlSchemaType objects
     */
    public static GraphQLSchemaTypeListDTO fromGraphqlSchemaTypeListtoDTO(List<GraphqlSchemaType> typeList) {
        GraphQLSchemaTypeListDTO graphQLSchemaTypeListDTO = new GraphQLSchemaTypeListDTO();
        List<GraphQLSchemaTypeDTO> graphQLSchemaTypeDTOList = new ArrayList<>();
        for (GraphqlSchemaType graphqlSchemaType : typeList) {
            GraphQLSchemaTypeDTO graphQLSchemaTypeDTO = new GraphQLSchemaTypeDTO();
            List<String> fieldList = new ArrayList<>(graphqlSchemaType.getFieldList());
            graphQLSchemaTypeDTO.setType(graphqlSchemaType.getType());
            graphQLSchemaTypeDTO.setFieldList(fieldList);
            graphQLSchemaTypeDTOList.add(graphQLSchemaTypeDTO);
        }
        graphQLSchemaTypeListDTO.setTypeList(graphQLSchemaTypeDTOList);
        return graphQLSchemaTypeListDTO;
    }

}
