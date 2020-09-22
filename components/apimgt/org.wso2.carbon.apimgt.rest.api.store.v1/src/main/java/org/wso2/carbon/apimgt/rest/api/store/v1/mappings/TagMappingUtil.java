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
package org.wso2.carbon.apimgt.rest.api.store.v1.mappings;

import org.wso2.carbon.apimgt.api.model.Tag;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.TagDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.TagListDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for mapping APIM core tag related objects into REST API tag related DTOs
 *
 */
public class TagMappingUtil {
    /**
     * Converts a Tag object into TagDTO
     *
     * @param tag Tag object
     * @return TagDTO corresponds to Tag object
     */
    public static TagDTO fromTagToDTO(Tag tag){
        TagDTO tagDTO = new TagDTO();
        tagDTO.setValue(tag.getName());
        tagDTO.setCount(tag.getNoOfOccurrences());
        return tagDTO;
    }

    /**
     * Converts a List object of Tags into a DTO
     *
     * @param tags  a list of Tag objects
     * @param limit  max number of objects returned
     * @param offset starting index
     * @return TierListDTO object containing TierDTOs
     */
    public static TagListDTO fromTagListToDTO(List<Tag> tags, int limit, int offset) {
        TagListDTO tagListDTO = new TagListDTO();
        List<TagDTO> tierDTOs = tagListDTO.getList();
        if (tierDTOs == null) {
            tierDTOs = new ArrayList<>();
            tagListDTO.setList(tierDTOs);
        }

        //identifying the proper start and end indexes
        int size = tags.size();
        int start = offset < size && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= size - 1 ? offset + limit - 1 : size - 1;

        tags.sort(Comparator.comparing(Tag::getNoOfOccurrences).reversed());

        List<Tag> paginatedTags = tags.subList(start, end);
        paginatedTags.sort(Comparator.comparing(Tag::getName));

        for (Tag tag : paginatedTags) {
            tierDTOs.add(fromTagToDTO(tag));
        }
        tagListDTO.setCount(tierDTOs.size());
        return tagListDTO;
    }

    /**
     * Sets pagination urls for a TagListDTO object given pagination parameters and url parameters
     *
     * @param tagListDTO a TagListDTO object
     * @param limit       max number of objects returned
     * @param offset      starting index
     * @param size        max offset
     */
    public static void setPaginationParams(TagListDTO tagListDTO, int limit, int offset, int size) {

        String paginatedPrevious = "";
        String paginatedNext = "";

        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, size);

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiUtil
                    .getTagsPaginatedURL(
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT));
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiUtil
                    .getTagsPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT));
        }

        PaginationDTO paginationDTO = CommonMappingUtil
                .getPaginationDTO(limit, offset, size, paginatedNext, paginatedPrevious);
        tagListDTO.setPagination(paginationDTO);
    }
}
