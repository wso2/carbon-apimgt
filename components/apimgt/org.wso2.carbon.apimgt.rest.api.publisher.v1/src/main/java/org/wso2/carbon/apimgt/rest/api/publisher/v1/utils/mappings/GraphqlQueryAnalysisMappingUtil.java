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
package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings;

import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import java.util.ArrayList;
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
        graphQLQueryComplexityInfoDTO.setMaxComplexity(graphqlComplexityInfo.getMaxComplexity());
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
        graphqlComplexityInfo.setMaxComplexity(graphQLQueryComplexityInfoDTO.getMaxComplexity());
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
     * Converts a list of GraphqlDepthInfo objects into a DTO object
     *
     * @param graphqlDepthInfoList List<GraphqlDepthInfo>
     * @return a new GraphQLQueryDepthInfoListDTO object corresponding to given list of GraphqlDepthInfo objects
     */
    public static GraphQLQueryDepthInfoListDTO fromGraphqlDepthInfoListtoDTO(
            List<GraphqlDepthInfo> graphqlDepthInfoList) {
        GraphQLQueryDepthInfoListDTO graphQLQueryDepthInfoListDTO = new GraphQLQueryDepthInfoListDTO();
        List<GraphQLQueryDepthInfoDTO> graphQLQueryDepthInfoDTOList = new ArrayList<GraphQLQueryDepthInfoDTO>();
        for (GraphqlDepthInfo graphqlDepthInfo : graphqlDepthInfoList) {
            GraphQLQueryDepthInfoDTO graphQLQueryDepthInfoDTO = new GraphQLQueryDepthInfoDTO();
            graphQLQueryDepthInfoDTO.setUuid(graphqlDepthInfo.getUuid());
            graphQLQueryDepthInfoDTO.setRole(graphqlDepthInfo.getRole());
            graphQLQueryDepthInfoDTO.setDepthValue(graphqlDepthInfo.getDepthValue());
            graphQLQueryDepthInfoDTOList.add(graphQLQueryDepthInfoDTO);
        }
        graphQLQueryDepthInfoListDTO.setList(graphQLQueryDepthInfoDTOList);
        return graphQLQueryDepthInfoListDTO;
    }

    /**
     * Converts a GraphQLQueryDepthInfo DTO object into a GraphqlDepthInfo object
     *
     * @param graphQLQueryDepthInfoDTO GraphQLQueryDepthInfoDTO object
     * @return a new GraphqlDepthInfo object corresponding to given GraphQLQueryDepthInfoDTO object
     */
    public static GraphqlDepthInfo fromDTOtoGraphqlDepthInfo(GraphQLQueryDepthInfoDTO graphQLQueryDepthInfoDTO) {
        GraphqlDepthInfo graphqlDepthInfo = new GraphqlDepthInfo();
        graphqlDepthInfo.setRole(graphQLQueryDepthInfoDTO.getRole());
        graphqlDepthInfo.setDepthValue(graphQLQueryDepthInfoDTO.getDepthValue());
        return graphqlDepthInfo;
    }

    /**
     * Converts a GraphqlDepthInfo object into a DTO object
     *
     * @param graphqlDepthInfo GraphqlDepthInfo object
     * @return a new GraphQLQueryDepthInfoDTO object corresponding to given GraphqlDepthInfo object
     */
    public static GraphQLQueryDepthInfoDTO fromGraphqlDepthInfotoDTO(GraphqlDepthInfo graphqlDepthInfo) {
        GraphQLQueryDepthInfoDTO graphQLQueryDepthInfoDTO = new GraphQLQueryDepthInfoDTO();
        graphQLQueryDepthInfoDTO.setUuid(graphqlDepthInfo.getUuid());
        graphQLQueryDepthInfoDTO.setRole(graphqlDepthInfo.getRole());
        graphQLQueryDepthInfoDTO.setDepthValue(graphqlDepthInfo.getDepthValue());
        return graphQLQueryDepthInfoDTO;
    }

    /**
     * Converts a GraphqlDepthComplexityStatus object into a DTO object
     *
     * @param graphqlDepthComplexityStatus GraphqlDepthComplexityStatus object
     * @return a new GraphQLDepthComplexityStatusDTO object corresponding to given GraphqlDepthComplexityStatus object
     */
    public static GraphQLDepthComplexityStatusDTO fromGraphqlDepthComplexityStatustoDTO(
            GraphqlDepthComplexityStatus graphqlDepthComplexityStatus) {
        GraphQLDepthComplexityStatusDTO graphQLDepthComplexityStatusDTO = new GraphQLDepthComplexityStatusDTO();
        graphQLDepthComplexityStatusDTO.setDepthEnabled(graphqlDepthComplexityStatus.getDepthEnabled());
        graphQLDepthComplexityStatusDTO.setComplexityEnabled(graphqlDepthComplexityStatus.getComplexityEnabled());
        return graphQLDepthComplexityStatusDTO;
    }

    /**
     * Converts a GraphQLDepthComplexityStatusDTO object into a GraphqlDepthComplexityStatus object
     *
     * @param body GraphQLDepthComplexityStatusDTO object
     * @return a new GraphqlDepthComplexityStatus object corresponding to given GraphQLDepthComplexityStatusDTO object
     */
    public static GraphqlDepthComplexityStatus fromDTOtoGraphqlDepthComplexityStatus(
            GraphQLDepthComplexityStatusDTO body) {
        GraphqlDepthComplexityStatus graphqlDepthComplexityStatus = new GraphqlDepthComplexityStatus();
        graphqlDepthComplexityStatus.setDepthEnabled(body.isDepthEnabled());
        graphqlDepthComplexityStatus.setComplexityEnabled(body.isComplexityEnabled());
        return graphqlDepthComplexityStatus;
    }
}
