/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APICategoryDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APICategoryListDTO;

import java.util.ArrayList;
import java.util.List;

public class APICategoryMappingUtil {

    /**
     * Convert list of API Categories to CategoryListDTO
     *
     * @param categories List of api categories
     * @return CategoryListDTO list containing api category data
     */
    public static APICategoryListDTO fromCategoryListToCategoryListDTO(List<APICategory> categories) {
        APICategoryListDTO categoryListDTO = new APICategoryListDTO();
        categoryListDTO.setCount(categories.size());
        categoryListDTO.setList(fromCategoryListToCategoryDTOList(categories));
        return categoryListDTO;
    }

    /**
     * Converts api category List to CategoryDTO List.
     *
     * @param categories List of api categories
     * @return CategoryDTO list
     */
    private static List<APICategoryDTO> fromCategoryListToCategoryDTOList(List<APICategory> categories) {
        List<APICategoryDTO> categoryDTOs = new ArrayList<>();
        for (APICategory category : categories) {
            APICategoryDTO categoryDTO = new APICategoryDTO();
            categoryDTO.setId(category.getId());
            categoryDTO.setName(category.getName());
            categoryDTO.setDescription(category.getDescription());
            categoryDTOs.add(categoryDTO);
        }
        return categoryDTOs;
    }
}
