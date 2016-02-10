/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.apimgt.api;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.util.Map;

public class FaultGatewaysException extends Exception {
	private Map<String, Map<String, String>> faultMap;
	public static final String PUBLISHED = "PUBLISHED";
	public static final String UN_PUBLISHED = "UNPUBLISHED";
	public String getFaultGateWayString() {
		String failedJson = "{\""+PUBLISHED+"\" : \"\" ,"+UN_PUBLISHED+":\"\"}";
		if (faultMap != null) {
			if (!faultMap.isEmpty()) {
				StringBuilder failedToPublish = new StringBuilder();
				StringBuilder failedToUnPublish = new StringBuilder();
				Map<String, String> failedToPublishMap = faultMap.get(PUBLISHED);
				Map<String, String> failedToUnPublishMap = faultMap.get(UN_PUBLISHED);
				for (Map.Entry<String, String> environmentEntry : failedToPublishMap.entrySet()) {
					failedToPublish.append(environmentEntry.getKey()).append(':').append(environmentEntry.getValue())
							.append(',');
				}
				for (Map.Entry<String, String> environmentEntry : failedToUnPublishMap.entrySet()) {
					failedToUnPublish.append(environmentEntry.getKey()).append(':').append(environmentEntry.getValue())
							.append(',');
				}
				if (failedToPublish.length() != 0) {
					failedToPublish.deleteCharAt(failedToPublish.length() - 1);
				}
				if (failedToUnPublish.length() != 0) {
					failedToUnPublish.deleteCharAt(failedToUnPublish.length() - 1);
				}
				failedJson = "{\""+PUBLISHED+"\" : \"" + failedToPublish.toString() + "\" ,\""+UN_PUBLISHED+"\":\"" +
				             failedToUnPublish.toString() + "\"}";
			}
		}
		return failedJson;
	}

	public void setFaultMap(Map<String, Map<String, String>> faultMap) {
		this.faultMap = faultMap;
	}

	public FaultGatewaysException(Map<String, Map<String, String>> faultMap) {
		this.faultMap = faultMap;
	}

	public String getFaultMap() {
		return JSONObject.toJSONString(faultMap);
	}
}
