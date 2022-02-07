/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APICategoryDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APICategoryListDTO;

public class APICategoryMappingUtilTestCase {

    @Test
    public void testCategoryDescription() throws Exception {
        APICategory category = new APICategory();
        String description = "sample description";
        category.setDescription(description);
        category.setName("test");
        List<APICategory> categories = new ArrayList<APICategory>();
        categories.add(category);

        APICategoryListDTO ListDto = APICategoryMappingUtil.fromCategoryListToCategoryListDTO(categories);
        APICategoryDTO dto = ListDto.getList().get(0);
        Assert.assertEquals("Category description mismatch", description, dto.getDescription());
    }

    @Test
    public void testCategoryDescriptionNull() throws Exception {
        APICategory category = new APICategory();
        category.setDescription(null);
        category.setName("test");
        List<APICategory> categories = new ArrayList<APICategory>();
        categories.add(category);

        APICategoryListDTO ListDto = APICategoryMappingUtil.fromCategoryListToCategoryListDTO(categories);
        APICategoryDTO dto = ListDto.getList().get(0);
        Assert.assertEquals("Category is null", "", dto.getDescription());

    }
}
