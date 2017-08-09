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
import org.wso2.carbon.apimgt.core.models.CompositeAPI;
import org.wso2.carbon.apimgt.rest.api.store.common.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.rest.api.store.dto.CompositeAPIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.CompositeAPIListDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;

;

public class CompositeAPIMappingUtilTestCase {

    @Test
    public void testToCompositeAPIDTOAndViceVersa() {
        Set<String> labels = new HashSet<>();
        labels.add("label1");
        //Test compositeAPI to CompositeAPIDTO
        CompositeAPI compositeAPI = SampleTestObjectCreator.createCompositeAPIModelBuilder().labels(labels).build();
        CompositeAPIDTO compositeAPIDTO = CompositeAPIMappingUtil.toCompositeAPIDTO(compositeAPI);
        assertEquals(compositeAPI.getId(), compositeAPIDTO.getId());
        assertEquals(compositeAPI.getName(), compositeAPIDTO.getName());
        assertEquals(compositeAPI.getProvider(), compositeAPIDTO.getProvider());
        assertEquals(compositeAPI.getVersion(), compositeAPIDTO.getVersion());
        assertEquals(compositeAPI.getContext(), compositeAPIDTO.getContext());
        assertEquals(compositeAPI.getDescription(), compositeAPIDTO.getDescription());
        assertEquals(compositeAPI.getLabels().size(), compositeAPIDTO.getLabels().size());
        assertEquals(compositeAPI.getApplicationId(), compositeAPIDTO.getApplicationId());

        //Test CompositeAPIDTO to compositeAPI
        CompositeAPI compositeAPIGenerated = CompositeAPIMappingUtil.toAPI(compositeAPIDTO).build();
        assertEquals(compositeAPIGenerated.getId(), compositeAPIDTO.getId());
        assertEquals(compositeAPIGenerated.getName(), compositeAPIDTO.getName());
        assertEquals(compositeAPIGenerated.getProvider(), compositeAPIDTO.getProvider());
        assertEquals(compositeAPIGenerated.getVersion(), compositeAPIDTO.getVersion());
        assertEquals(compositeAPIGenerated.getContext(), compositeAPIDTO.getContext());
        assertEquals(compositeAPIGenerated.getDescription(), compositeAPIDTO.getDescription());
        assertEquals(compositeAPIGenerated.getLabels().size(), compositeAPIDTO.getLabels().size());
        assertEquals(compositeAPIGenerated.getApplicationId(), compositeAPIDTO.getApplicationId());
    }

    @Test
    public void testToCompositeAPIListDTO() {
        List<CompositeAPI> apisResult = new ArrayList<>();
        CompositeAPI comp1 = SampleTestObjectCreator.createCompositeAPIModelBuilder().name("newComp1").build();
        CompositeAPI comp2 = SampleTestObjectCreator.createCompositeAPIModelBuilder().name("newComp2").build();
        apisResult.add(comp1);
        apisResult.add(comp2);
        CompositeAPIListDTO compositeAPIListDTO = CompositeAPIMappingUtil.toCompositeAPIListDTO(apisResult);
        assertEquals(compositeAPIListDTO.getCount(), (Integer) apisResult.size());
        assertEquals(comp1.getId(), compositeAPIListDTO.getList().get(0).getId());
        assertEquals(comp1.getName(), compositeAPIListDTO.getList().get(0).getName());
        assertEquals(comp1.getProvider(), compositeAPIListDTO.getList().get(0).getProvider());
        assertEquals(comp1.getVersion(), compositeAPIListDTO.getList().get(0).getVersion());
        assertEquals(comp1.getContext(), compositeAPIListDTO.getList().get(0).getContext());
        assertEquals(comp1.getDescription(), compositeAPIListDTO.getList().get(0).getDescription());
        assertEquals(comp1.getApplicationId(), compositeAPIListDTO.getList().get(0).getApplicationId());

        assertEquals(comp2.getId(), compositeAPIListDTO.getList().get(1).getId());
        assertEquals(comp2.getName(), compositeAPIListDTO.getList().get(1).getName());
        assertEquals(comp2.getProvider(), compositeAPIListDTO.getList().get(1).getProvider());
        assertEquals(comp2.getVersion(), compositeAPIListDTO.getList().get(1).getVersion());
        assertEquals(comp2.getContext(), compositeAPIListDTO.getList().get(1).getContext());
        assertEquals(comp2.getDescription(), compositeAPIListDTO.getList().get(1).getDescription());
        assertEquals(comp2.getApplicationId(), compositeAPIListDTO.getList().get(1).getApplicationId());
    }
}
