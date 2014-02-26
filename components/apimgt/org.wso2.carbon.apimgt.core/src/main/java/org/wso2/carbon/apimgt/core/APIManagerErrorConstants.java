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

/**
 * Include all the constants related to API access violations that may have occured
 * while validating security requirements & throttling requirements.
 */
public class APIManagerErrorConstants {
	
	public static final int API_AUTH_GENERAL_ERROR       = 900900;
    public static final String API_AUTH_GENERAL_ERROR_MESSAGE = "Unclassified Authentication Failure";

    public static final int API_AUTH_INVALID_CREDENTIALS = 900901;
    public static final String API_AUTH_INVALID_CREDENTIALS_MESSAGE = "Invalid Credentials";

    public static final int API_AUTH_MISSING_CREDENTIALS = 900902;
    public static final String API_AUTH_MISSING_CREDENTIALS_MESSAGE = "Missing Credentials";

    public static final int API_AUTH_ACCESS_TOKEN_EXPIRED = 900903;
    public static final String API_AUTH_ACCESS_TOKEN_EXPIRED_MESSAGE = "Access Token Expired";

    public static final int API_AUTH_ACCESS_TOKEN_INACTIVE = 900904;
    public static final String API_AUTH_ACCESS_TOKEN_INACTIVE_MESSAGE = "Access Token Inactive";

    public static final int API_AUTH_INCORRECT_ACCESS_TOKEN_TYPE = 900905;
    public static final String API_AUTH_INCORRECT_ACCESS_TOKEN_TYPE_MESSAGE = "Incorrect Access Token Type is provided";

    public static final int API_AUTH_INCORRECT_API_RESOURCE = 900906;
    public static final String API_AUTH_INCORRECT_API_RESOURCE_MESSAGE = "No matching resource found in the API for the given request";

    public static final int API_BLOCKED = 900907;
    public static final String API_BLOCKED_MESSAGE = "The requested API is temporarily blocked";

    public static final int API_AUTH_FORBIDDEN = 900908;
    public static final String API_AUTH_FORBIDDEN_MESSAGE = "Resource forbidden ";
    
    public static final int API_THROTTLE_OUT = 900800;
    public static final String API_THROTTLE_OUT_MESSAGE = "Message Throttled Out";
    
    public static final String API_SECURITY_NS = "http://wso2.org/apimanager/security";
    public static final String API_SECURITY_NS_PREFIX = "ams";
    
    public static final String API_THROTTLE_NS = "http://wso2.org/apimanager/throttling";
    public static final String API_THROTTLE_NS_PREFIX = "amt";

    /**
     * returns an String that corresponds to errorCode passed in
     * @param errorCode
     * @return String
     */
    public static final String getFailureMessage(int errorCode){
        String errorMessage;
        switch (errorCode){
            case API_AUTH_ACCESS_TOKEN_EXPIRED:
                errorMessage = API_AUTH_ACCESS_TOKEN_EXPIRED_MESSAGE;
            break;
            case API_AUTH_ACCESS_TOKEN_INACTIVE:
                errorMessage = API_AUTH_ACCESS_TOKEN_INACTIVE_MESSAGE;
            break;
            case API_AUTH_GENERAL_ERROR:
                errorMessage = API_AUTH_GENERAL_ERROR_MESSAGE;
            break;
            case API_AUTH_INVALID_CREDENTIALS:
                errorMessage = API_AUTH_INVALID_CREDENTIALS_MESSAGE;
            break;
            case API_AUTH_MISSING_CREDENTIALS:
                errorMessage = API_AUTH_MISSING_CREDENTIALS_MESSAGE;
            break;
            case API_AUTH_INCORRECT_API_RESOURCE:
                errorMessage = API_AUTH_INCORRECT_API_RESOURCE_MESSAGE;
            break;
            case API_AUTH_INCORRECT_ACCESS_TOKEN_TYPE:
                errorMessage = API_AUTH_INCORRECT_ACCESS_TOKEN_TYPE_MESSAGE;
            break;
            case API_BLOCKED:
                errorMessage = API_BLOCKED_MESSAGE;
            break;
            case API_AUTH_FORBIDDEN:
                errorMessage = API_AUTH_FORBIDDEN_MESSAGE;
                break;
            case API_THROTTLE_OUT:
                errorMessage = API_THROTTLE_OUT_MESSAGE;
                break;
            default:
                errorMessage = API_AUTH_GENERAL_ERROR_MESSAGE;
            break;
        }
        return errorMessage;
    }
       

}
