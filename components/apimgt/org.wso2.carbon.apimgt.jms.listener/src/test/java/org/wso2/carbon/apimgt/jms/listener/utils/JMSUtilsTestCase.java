/*
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
package org.wso2.carbon.apimgt.jms.listener.utils;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.common.jms.utils.JMSUtils;

import java.util.Hashtable;

public class JMSUtilsTestCase {

	@Test
	public void testMaskAxis2ConfigSensitiveParameters() {
		Hashtable<String, String> sensitiveParamsTable = new Hashtable<String, String>();
		sensitiveParamsTable.put("connectionfactory.TopicConnectionFactory",
				"amqp://admin:admin@clientid/carbon?brokerlist='tcp://localhost:5672'");

		Hashtable<String, String> maskedParamTable = JMSUtils.maskAxis2ConfigSensitiveParameters(sensitiveParamsTable);
		Assert.assertEquals("amqp://***:***@clientid/carbon?brokerlist='tcp://localhost:5672'",
				maskedParamTable.get("connectionfactory.TopicConnectionFactory"));

		sensitiveParamsTable = new Hashtable<String, String>();
		sensitiveParamsTable.put("connectionfactory.TopicConnectionFactory",
				"amqp://admin:%23admin@clientid/carbon?brokerlist='tcp://localhost:5672'");
		maskedParamTable = JMSUtils.maskAxis2ConfigSensitiveParameters(sensitiveParamsTable);
		Assert.assertEquals("amqp://***:***@clientid/carbon?brokerlist='tcp://localhost:5672'",
				maskedParamTable.get("connectionfactory.TopicConnectionFactory"));
	}
}
