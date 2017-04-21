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

package org.wso2.carbon.apimgt.core.dao.impl;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.apimgt.core.TestUtil;
import org.wso2.carbon.apimgt.core.dao.TagDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.Tag;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TagDAOImplIT extends DAOIntegrationTestBase {

    @Test(description = "Get all tags")
    public void testGetTags() throws APIManagementException {
        //add tags by creating two different APIs
        API api = TestUtil.addTestAPI();
        API alternativeApi = TestUtil.addAlternativeAPI();

        //get the list of all tags from DB
        TagDAO tag = DAOFactory.getTagDAO();
        List<Tag> tagList = tag.getTags();
        Assert.assertNotNull(tagList);

        //check tags for correctness
        HashSet<String> set = new HashSet<>();
        set.addAll(api.getTags());
        set.addAll(alternativeApi.getTags());

        List<String> tagsFromDB = new ArrayList<>();
        for (Tag availableTag : tagList) {
            tagsFromDB.add(availableTag.getName());
        }
        Assert.assertTrue(set.containsAll(tagsFromDB));
        Assert.assertTrue(set.size() == tagsFromDB.size());
    }
}
