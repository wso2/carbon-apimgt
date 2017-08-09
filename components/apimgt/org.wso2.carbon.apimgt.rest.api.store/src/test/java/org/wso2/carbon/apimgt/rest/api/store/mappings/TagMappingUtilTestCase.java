/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.store.mappings;

import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.models.Tag;
import org.wso2.carbon.apimgt.rest.api.store.dto.TagDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.TagListDTO;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class TagMappingUtilTestCase {

    @Test
    public void testFromTagToDTO() {
        Tag.Builder tagBuilder = new Tag.Builder();
        Tag tag = tagBuilder.name("tag1").count(1).build();
        TagDTO tagDTO = TagMappingUtil.fromTagToDTO(tag);
        assertEquals(tag.getName(), tagDTO.getName());
        assertEquals((Integer) tag.getCount(), tagDTO.getWeight());
    }

    @Test
    public void testFromTagListToDTO() {
        Tag.Builder tagBuilder = new Tag.Builder();
        Tag tag1 = tagBuilder.name("tag1").count(2).build();
        Tag tag2 = tagBuilder.name("tag2").count(2).build();
        List<Tag> tags = new ArrayList<>();
        tags.add(tag1);
        tags.add(tag2);
        TagListDTO tagListDTO = TagMappingUtil.fromTagListToDTO(tags, 10, 0);
        assertEquals((Integer) tags.size(), tagListDTO.getCount());
        assertEquals(tagListDTO.getList().get(0).getName(), tag1.getName());
        assertEquals(tagListDTO.getList().get(0).getWeight(), (Integer) tag1.getCount());
        assertEquals(tagListDTO.getList().get(1).getName(), tag2.getName());
        assertEquals(tagListDTO.getList().get(1).getWeight(), (Integer) tag2.getCount());
    }
}
