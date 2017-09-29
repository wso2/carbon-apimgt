/*
 *
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * /
 */
package org.wso2.carbon.apimgt.impl.util.test;

import org.junit.Test;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import junit.framework.Assert;

public class APIUtilTestCase {
	@Test
	public void testgetSingleSearchCriteriaForTags() throws Exception {
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

}
