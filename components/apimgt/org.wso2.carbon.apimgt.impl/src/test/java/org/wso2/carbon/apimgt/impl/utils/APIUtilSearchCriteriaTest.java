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

package org.wso2.carbon.apimgt.impl.utils;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIManagementException;

public class APIUtilSearchCriteriaTest {
    @Test
    public void testGetSingleSearchCriteriaForNameVersion() throws Exception {
        String result = APIUtil.getSingleSearchCriteria("version:1.0");

        Assert.assertEquals("Invalid solr format for search query", "version=*1.0*", result);

        result = APIUtil.getSingleSearchCriteria("name:john");

        Assert.assertEquals("Invalid solr format for search query","name=*john*", result);
    }

    @Test
    public void testGetSingleSearchCriteriaForTags() throws Exception {
        String queryString1 = "tags:tag1";
        String queryString2 = "tag:tag1";

        String expectedSearchQuery = "tags=tag1";
        String searchString;
        //Both should result in same search query.
        searchString = APIUtil.getSingleSearchCriteria(queryString1);
        Assert.assertEquals("Invalid tag search query", expectedSearchQuery, searchString);

        searchString = APIUtil.getSingleSearchCriteria(queryString2);
        Assert.assertEquals("Invalid tag search query", expectedSearchQuery, searchString);
    }

    @Test
    public void testGetSingleSearchCriteriaInvalidSearch() throws Exception {
        try {
            APIUtil.getSingleSearchCriteria(":");
            // Fail if exception was not thrown
            Assert.assertTrue("Exception not thrown for invalid search",false);
        } catch (APIManagementException e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testGetSingleSearchCriteriaForProvider() throws Exception {
        String result = APIUtil.getSingleSearchCriteria("provider:user1");

        Assert.assertEquals("Invalid solr format for search query", "provider=*user1*", result);

        result = APIUtil.getSingleSearchCriteria("provider:john@doe.com");

        Assert.assertEquals("Invalid solr format for search query","provider=*john-AT-doe.com*", result);
    }

    @Test
    public void testGetSingleSearchCriteriaGeneric() throws Exception {
        String result = APIUtil.getSingleSearchCriteria("TestText");

        Assert.assertEquals("Invalid solr format for search query", "name=*TestText*", result);
    }

    @Test
    public void testGetORBasedSearchCriteria() throws Exception {
        String[] values = {"one", "two", "three"};
        String expectedValue = "(one OR two OR three)";

        String searchCriteria = APIUtil.getORBasedSearchCriteria(values);

        Assert.assertEquals("Invalid search criteria", expectedValue, searchCriteria);

        values = new String[]{"single"};
        expectedValue = "(single)";

        searchCriteria = APIUtil.getORBasedSearchCriteria(values);

        Assert.assertEquals("Invalid search criteria", expectedValue, searchCriteria);

    }

    @Test
    public void testGetSingleSearchCriteriaForTagsWithSpace() throws Exception {
        String queryString1 = "tag:tag 1";

        String expectedSearchQuery = "tags=tag\\ 1";
        String searchString;
        //Both should result in same search query.
        searchString = APIUtil.getSingleSearchCriteria(queryString1);
        Assert.assertEquals("Invalid tag search query", expectedSearchQuery, searchString);
    }
}
