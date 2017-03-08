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
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.Label;

import java.util.ArrayList;
import java.util.List;

public class LabelDAOImplIT extends DAOIntegrationTestBase {

    @Test
    public void testAddGetLabels() throws Exception {
        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        Label label1 = SampleTestObjectCreator.createLabel("public", "https://test.public");
        Label label2 = SampleTestObjectCreator.createLabel("private", "https://test.private");
        List<Label> labelList = new ArrayList<>();
        labelList.add(label1);
        labelList.add(label2);
        labelDAO.addLabels(labelList);

        List<Label> labelsFromDb = labelDAO.getLabels();
        Assert.assertNotNull(labelsFromDb);
        Assert.assertEquals(labelsFromDb.size(), 2);
    }

    @Test
    public void testAddDuplicateLabelName() throws Exception {
        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        Label label1 = SampleTestObjectCreator.createLabel("public", "https://test.public");
        List<Label> labelList = new ArrayList<>();
        labelList.add(label1);
        labelDAO.addLabels(labelList);

        Label label2 = SampleTestObjectCreator.createLabel("public", "https://test.public");
        labelList.add(label2);

        try {
            labelDAO.addLabels(labelList);
            Assert.fail("Exception not thrown for adding duplicate Label");
        } catch (APIMgtDAOException e) {
            // Just catch the exception so that we can continue execution
        }

        List<Label> labelsFromDb = labelDAO.getLabels();
        Assert.assertNotNull(labelsFromDb);
        Assert.assertEquals(labelsFromDb.size(), 1);
    }

    @Test
    public void testGetLabelByName() throws Exception {

        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        Label label = SampleTestObjectCreator.createLabel("public", "https://test.public");
        List<Label> labelList = new ArrayList<>();
        labelList.add(label);
        labelDAO.addLabels(labelList);

        Label labelFromDb = labelDAO.getLabelByName(label.getName());
        Assert.assertNotNull(labelFromDb);
        Assert.assertEquals(labelFromDb, label);
    }

    @Test
    public void testGetLabelsByName() throws Exception {

        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        Label label1 = SampleTestObjectCreator.createLabel("public", "https://test.public");
        Label label2 = SampleTestObjectCreator.createLabel("private", "https://test.private");
        List<Label> labelList = new ArrayList<>();
        labelList.add(label1);
        labelList.add(label2);
        labelDAO.addLabels(labelList);

        List<String> labelNames = new ArrayList<>();
        labelNames.add(label1.getName());
        labelNames.add(label2.getName());

        List<Label> labelFromDb = labelDAO.getLabelsByName(labelNames);
        Assert.assertNotNull(labelFromDb);
        Assert.assertEquals(labelFromDb.size(), 2);
    }

    @Test
    public void testDeleteLabel() throws Exception {

        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        Label label = SampleTestObjectCreator.createLabel("public", "https://test.public");
        List<Label> labelList = new ArrayList<>();
        labelList.add(label);
        labelDAO.addLabels(labelList);

        labelDAO.deleteLabel(label.getName());
        Label labelFromDb = labelDAO.getLabelByName(label.getName());
        Assert.assertNull(labelFromDb);
    }

}
