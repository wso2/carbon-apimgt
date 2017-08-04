/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.rest.api.analytics.mapping;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.models.analytics.ApplicationCount;
import org.wso2.carbon.apimgt.rest.api.analytics.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.rest.api.analytics.dto.ApplicationCountListDTO;
import org.wso2.carbon.apimgt.rest.api.analytics.mappings.AnalyticsMappingUtil;

import java.util.ArrayList;
import java.util.List;

public class AnalyticsMappingUtilTestCase {

    @Test
    public void fromApplicationCountToListDTOTest() {
        List<ApplicationCount> applicationCountList = new ArrayList<>();
        applicationCountList.add(SampleTestObjectCreator.createRandomApplicationCountObject());
        applicationCountList.add(SampleTestObjectCreator.createRandomApplicationCountObject());
        applicationCountList.add(SampleTestObjectCreator.createRandomApplicationCountObject());
        ApplicationCountListDTO applicationCountListDTO = AnalyticsMappingUtil.
                fromApplicationCountToListDTO(applicationCountList);

        Assert.assertEquals(applicationCountList.size(), applicationCountListDTO.getList().size());
        for (int i = 0; i < applicationCountList.size(); i++) {
            Assert.assertEquals(Long.valueOf(applicationCountList.get(i).getTimestamp()),
                    Long.valueOf(applicationCountListDTO.getList().get(i).getTime()));
        }

    }

}
