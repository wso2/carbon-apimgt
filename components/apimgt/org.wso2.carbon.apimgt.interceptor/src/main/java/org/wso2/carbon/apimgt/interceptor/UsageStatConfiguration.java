/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */
package org.wso2.carbon.apimgt.interceptor;

import org.wso2.carbon.apimgt.usage.publisher.APIMgtUsageDataPublisher;
/**
 * Set stat publishing related configuration
 *
 */
public class UsageStatConfiguration {

	private boolean statsPublishingEnabled = false;	
	private String hostName;
	private APIMgtUsageDataPublisher publisher;

	public UsageStatConfiguration() {

	}

	public boolean isStatsPublishingEnabled() {
		return statsPublishingEnabled;
	}

	public void setStatsPublishingEnabled(boolean statsPublishingEnabled) {
		this.statsPublishingEnabled = statsPublishingEnabled;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public APIMgtUsageDataPublisher getPublisher() {
		return publisher;
	}

	public void setPublisher(APIMgtUsageDataPublisher publisher) {
		this.publisher = publisher;		
	}

}
