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
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.rest.api.store.common.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.rest.api.store.dto.LabelListDTO;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class LabelMappingUtilTestCase {

    @Test
    public void testToLabelListDTO() {
        List<Label> labelList = new ArrayList<>();
        Label label1 = SampleTestObjectCreator.createLabel("label1").build();
        Label label2 = SampleTestObjectCreator.createLabel("label2").build();
        labelList.add(label1);
        labelList.add(label2);
        LabelListDTO labelListDTO = LabelMappingUtil.toLabelListDTO(labelList);
        assertEquals(labelListDTO.getCount(), (Integer) labelList.size());
        assertEquals(labelListDTO.getList().get(0).getName(), label1.getName());
        assertEquals(labelListDTO.getList().get(0).getAccessUrls(), label1.getAccessUrls());
        assertEquals(labelListDTO.getList().get(0).getLabelId(), label1.getId());
        assertEquals(labelListDTO.getList().get(1).getName(), label2.getName());
        assertEquals(labelListDTO.getList().get(1).getAccessUrls(), label2.getAccessUrls());
        assertEquals(labelListDTO.getList().get(1).getLabelId(), label2.getId());

    }
}
