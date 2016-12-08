/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.rest.api.store.mappings;

import org.wso2.carbon.apimgt.core.models.Tag;
import org.wso2.carbon.apimgt.rest.api.store.dto.TagDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.TagListDTO;

import java.util.ArrayList;
import java.util.List;

public class TagMappingUtil {
    /**
     * Converts a Tag object into TagDTO
     *
     * @param tag Tag object
     * @return TagDTO corresponds to Tag object
     */
    public static TagDTO fromTagToDTO(Tag tag){
        TagDTO tagDTO = new TagDTO();
        tagDTO.setName(tag.getName());
        tagDTO.setWeight(tag.getCount());
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

        for (int i = start; i <= end; i++) {
            Tag tag = tags.get(i);
            tierDTOs.add(fromTagToDTO(tag));
        }
        tagListDTO.setCount(tierDTOs.size());
        return tagListDTO;
    }
}
