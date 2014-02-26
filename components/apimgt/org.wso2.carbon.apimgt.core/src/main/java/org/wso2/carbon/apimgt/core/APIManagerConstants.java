/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.apimgt.core;

public class APIManagerConstants {
	
	public static final String APPLICATION_THROTTLE_POLICY_KEY = "gov:/apimgt/applicationdata/tiers.xml";

    public static final String GOVERNANCE_REGISTRY_PREFIX = "gov:";
    
    /*API Management configuration in carbon.xml */
    public static final String API_MANGEMENT = "APIManagement.";
        
    public static final String API_MANGEMENT_ENABLED = API_MANGEMENT + "Enabled";
    
    public static final String EXTERNAL_API_MANGEMENT = API_MANGEMENT + "ExternalAPIManager.";
    
    public static final String EXTERNAL_API_GATEWAY = EXTERNAL_API_MANGEMENT + "APIGatewayURL";
    
    public static final String EXTERNAL_API_PUBLISHER = EXTERNAL_API_MANGEMENT + "APIPublisherURL";
    
    public static final String API_MANGEMENT_LOAD_CTX = API_MANGEMENT + "LoadAPIContextsInServerStartup";


}
