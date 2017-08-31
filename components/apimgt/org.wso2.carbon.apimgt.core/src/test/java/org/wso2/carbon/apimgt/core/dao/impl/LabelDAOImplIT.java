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
import java.util.Set;

public class LabelDAOImplIT extends DAOIntegrationTestBase {

    @Test
    public void testAddGetLabels() throws Exception {
        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        List<String> accessUrls = new ArrayList<>();
        accessUrls.add("https://test.public");
        accessUrls.add("http://test.public");
        Label label1 = SampleTestObjectCreator.createLabel("public").accessUrls(accessUrls).build();

        Label label2 = SampleTestObjectCreator.createLabel("private").build();
        List<Label> labelList = new ArrayList<>();
        labelList.add(label1);
        labelList.add(label2);
        labelDAO.addLabels(labelList);

        List<Label> labelsFromDb = labelDAO.getLabels();
        Assert.assertNotNull(labelsFromDb);
        Assert.assertEquals(labelsFromDb.size(), 3);
    }

    @Test
    public void testAddDuplicateLabelName() throws Exception {
        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        Label label1 = SampleTestObjectCreator.createLabel("public").build();
        List<Label> labelList = new ArrayList<>();
        labelList.add(label1);
        labelDAO.addLabels(labelList);

        Label label2 = SampleTestObjectCreator.createLabel("public").build();
        labelList.add(label2);

        try {
            labelDAO.addLabels(labelList);
            Assert.fail("Exception not thrown for adding duplicate labels");
        } catch (APIMgtDAOException e) {
            // Just catch the exception so that we can continue execution
        }

        List<Label> labelsFromDb = labelDAO.getLabels();
        Assert.assertNotNull(labelsFromDb);
        Assert.assertEquals(labelsFromDb.size(), 2);
    }

    @Test
    public void testGetLabelByName() throws Exception {

        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        Label label = SampleTestObjectCreator.createLabel("public").build();
        List<Label> labelList = new ArrayList<>();
        labelList.add(label);
        labelDAO.addLabels(labelList);

        Label labelFromDb = labelDAO.getLabelByName(label.getName());
        Assert.assertNotNull(labelFromDb);
        Assert.assertEquals(labelFromDb, label);
    }

    @Test
    public void testGetLabelsByNameForIncorrectLabelName() throws Exception {

        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        Label labelFromDb = labelDAO.getLabelByName("test");
        Assert.assertNull(labelFromDb);
    }

    @Test
    public void testGetLabelsByName() throws Exception {

        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        Label label1 = SampleTestObjectCreator.createLabel("public").build();
        Label label2 = SampleTestObjectCreator.createLabel("private").build();
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
    public void testGetLabelsByNameForIncorrectLabelNames() throws Exception {

        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        Label label1 = SampleTestObjectCreator.createLabel("public").build();
        Label label2 = SampleTestObjectCreator.createLabel("private").build();
        List<Label> labelList = new ArrayList<>();
        labelList.add(label1);
        labelList.add(label2);
        labelDAO.addLabels(labelList);

        List<String> labelNames = new ArrayList<>();
        labelNames.add("test");
        labelNames.add(label1.getName());

        List<Label> labelFromDb = labelDAO.getLabelsByName(labelNames);
        Assert.assertNotNull(labelFromDb);
        Assert.assertEquals(labelFromDb.size(), 1);
    }

    @Test
    public void testGetLabelNamesByIDs() throws Exception {

        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        Label label1 = SampleTestObjectCreator.createLabel("public").build();
        Label label2 = SampleTestObjectCreator.createLabel("private").build();
        List<Label> labelList = new ArrayList<>();
        labelList.add(label1);
        labelList.add(label2);
        labelDAO.addLabels(labelList);

        List<String> labelIds = new ArrayList<>();
        String labelId1 = LabelDAOImpl.getLabelID(label1.getName());
        String labelId2 = LabelDAOImpl.getLabelID(label2.getName());
        labelIds.add(labelId1);
        labelIds.add(labelId2);

        Set<String> labelNamesFromDb = LabelDAOImpl.getLabelNamesByIDs(labelIds);
        Assert.assertNotNull(labelNamesFromDb);
        Assert.assertEquals(labelNamesFromDb.size(), 2);
    }

    @Test
    public void testDeleteLabel() throws Exception {

        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        Label label = SampleTestObjectCreator.createLabel("public").build();
        List<Label> labelList = new ArrayList<>();
        labelList.add(label);
        labelDAO.addLabels(labelList);

        labelDAO.deleteLabel(label.getId());
        Label labelFromDb = labelDAO.getLabelByName(label.getName());
        Assert.assertNull(labelFromDb);
    }

    @Test
    public void testUpdateLabel() throws Exception {

        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        Label label = SampleTestObjectCreator.createLabel("public").build();
        List<Label> labelList = new ArrayList<>();
        labelList.add(label);
        labelDAO.addLabels(labelList);

        List<String> accessUrls = new ArrayList<>();
        accessUrls.add("https://updated.public");
        Label updatedLabel = SampleTestObjectCreator.createLabel("public").id(label.getId()).accessUrls(accessUrls)
                .build();

        labelDAO.updateLabel(updatedLabel);
        List<Label> labelsFromDb = labelDAO.getLabels();
        Assert.assertNotNull(labelsFromDb);
        Assert.assertEquals(labelsFromDb.size(), 2);
        Assert.assertTrue(labelsFromDb.contains(updatedLabel));
    }

}
