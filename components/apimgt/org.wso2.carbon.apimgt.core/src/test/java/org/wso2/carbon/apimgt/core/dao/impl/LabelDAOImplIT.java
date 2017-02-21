/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.core.dao.impl;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.SampleTestObjectCreator;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.models.Label;

import java.util.ArrayList;
import java.util.List;

public class LabelDAOImplIT extends DAOIntegrationTestBase{

    @Test
    public void testAddGetLabel() throws Exception
    {
        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        Label label = SampleTestObjectCreator.createLabel("public","https://localhost:8243");
        List<Label> labelList = new ArrayList<>();
        labelList.add(label);
        labelDAO.addLabels(labelList);

        List<String> labelNames = new ArrayList<>();
        labelNames.add(label.getName());
        List<Label> labelsFromDb = labelDAO.getLabelsByName(labelNames);

        Assert.assertNotNull(labelsFromDb);
        Assert.assertEquals(labelsFromDb.size(), 1);
        Assert.assertEquals(labelsFromDb, labelList);

    }
}
