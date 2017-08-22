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

public class LabelDAOImplIT extends DAOIntegrationTestBase {

    @Test
    public void testAddGetLabels() throws Exception {
        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        List<String> accessUrls = new ArrayList<>();
        accessUrls.add("https://test.public");
        accessUrls.add("http://test.public");
        Label label1 = SampleTestObjectCreator.createLabel("public").accessUrls(accessUrls).type("GATEWAY").build();
        LabelDAOImpl.addLabel(label1);

        Label label2 = SampleTestObjectCreator.createLabel("private").type("STORE").build();
        List<Label> labelList = new ArrayList<>();
        LabelDAOImpl.addLabel(label2);

        List<Label> labelsFromDb = labelDAO.getLabels();
        Assert.assertNotNull(labelsFromDb);
        Assert.assertEquals(labelsFromDb.size(), 3);
    }

    @Test
    public void testAddDuplicateLabelName() throws Exception {
       /* LabelDAO labelDAO = DAOFactory.getLabelDAO();
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
        Assert.assertEquals(labelsFromDb.size(), 2);*/
    }

    @Test
    public void testGetLabelById() throws Exception {

        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        Label label = SampleTestObjectCreator.createLabel("public").build();
        List<Label> labelList = new ArrayList<>();
        LabelDAOImpl.addLabel(label);

        Label labelFromDb = labelDAO.getLabelByID(label.getId());
        Assert.assertNotNull(labelFromDb);
        Assert.assertEquals(labelFromDb, label);
    }

    @Test
    public void testGetLabelsByIDForIncorrectLabelID() throws Exception {

        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        Label labelFromDb = labelDAO.getLabelByID("100000000002");
        Assert.assertNull(labelFromDb);
    }

    @Test
    public void testGetLabelsByID() throws Exception {

        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        Label label1 = SampleTestObjectCreator.createLabel("public").build();
        Label label2 = SampleTestObjectCreator.createLabel("private").build();
        List<Label> labelList = new ArrayList<>();
        LabelDAOImpl.addLabel(label1);
        LabelDAOImpl.addLabel(label2);


        List<String> labelIds = new ArrayList<>();
        labelIds.add(label1.getId());
        labelIds.add(label2.getId());

        List<Label> labelFromDb = new ArrayList<>();
        for (String id : labelIds) {
            labelFromDb.add(labelDAO.getLabelByID(id));
        }
        Assert.assertNotNull(labelFromDb);
        Assert.assertEquals(labelFromDb.size(), 2);
    }

    @Test
    public void testGetLabelsByNameForIncorrectLabelNames() throws Exception {

        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        Label label1 = SampleTestObjectCreator.createLabel("public").build();
        Label label2 = SampleTestObjectCreator.createLabel("private").build();
        LabelDAOImpl.addLabel(label1);
        LabelDAOImpl.addLabel(label2);


        List<String> labelIds = new ArrayList<>();
        labelIds.add("10000000003");
        labelIds.add(label2.getId());

        List<Label> labelFromDb = new ArrayList<>();
        for (String id : labelIds) {
                  labelFromDb.add(labelDAO.getLabelByID(id));
        }
        Assert.assertNotNull(labelFromDb);
        Assert.assertEquals(labelFromDb.size(), 2);
    }

    @Test
    public void testDeleteLabel() throws Exception {

        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        Label label = SampleTestObjectCreator.createLabel("public").build();
        LabelDAOImpl.addLabel(label);

        labelDAO.deleteLabel(label.getId());
        Label labelFromDb = labelDAO.getLabelByID(label.getId());
        Assert.assertNull(labelFromDb);
    }

    @Test
    public void testUpdateLabel() throws Exception {

        LabelDAO labelDAO = DAOFactory.getLabelDAO();
        Label label = SampleTestObjectCreator.createLabel("public").build();
        LabelDAOImpl.addLabel(label);

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
