/*
 *
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

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
     * Converts a GraphQLQueryComplexityInfo DTO object into a GraphqlComplexityInfo object
     *
     * @param graphQLQueryComplexityInfoDTO GraphQLQueryComplexityInfoDTO object
     * @return a new GraphqlComplexityInfo object corresponding to given GraphQLQueryComplexityInfoDTO object
     */
    public static GraphqlComplexityInfo fromDTOtoGraphqlComplexityInfo(
            GraphQLQueryComplexityInfoDTO graphQLQueryComplexityInfoDTO) {
        GraphqlComplexityInfo graphqlComplexityInfo = new GraphqlComplexityInfo();
        List<CustomComplexityDetails> customComplexityDetailsList = new ArrayList<CustomComplexityDetails>();
        for (GraphQLCustomComplexityInfoDTO graphQLCustomComplexityInfoDTO : graphQLQueryComplexityInfoDTO.getList()) {
            CustomComplexityDetails customComplexityDetails = new CustomComplexityDetails();
            customComplexityDetails.setType(graphQLCustomComplexityInfoDTO.getType());
            customComplexityDetails.setField(graphQLCustomComplexityInfoDTO.getField());
            customComplexityDetails.setComplexityValue(graphQLCustomComplexityInfoDTO.getComplexityValue());
            customComplexityDetailsList.add(customComplexityDetails);
        }
        graphqlComplexityInfo.setList(customComplexityDetailsList);
        return graphqlComplexityInfo;
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