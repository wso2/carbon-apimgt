/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.api.model.policy;

public class PolicyConstants {

    public static final String IP_RANGE_TYPE = "IPRange";

    public static final String IP_SPECIFIC_TYPE ="IPSpecific";

    public static final String HEADER_TYPE = "Header";

    public static final String DATE_RANGE_TYPE = "DateRage";

    public static final String DATE_SPECIFIC_TYPE = "DateSpecific";

    public static final String JWT_CLAIMS_TYPE = "JWTClaims";

    public static final String HTTP_VERB_TYPE = "HTTPVerb";

    public static final String QUERY_PARAMETER_TYPE = "QueryParameterType";

    public static final String BANDWIDTH_TYPE = "bandwidthVolume";

    public static final String REQUEST_COUNT_TYPE = "requestCount";

    public static final String EVENT_COUNT_TYPE = "eventCount";

    public static final String DATE_QUERY = "date";

    public static final String IP_QUERY = "ip";

    public static final String IPv6_QUERY = "ipv6";
    
    public static final String HTTP_VERB_QUERY = "verb";

    public static final String OPEN_BRACKET = "(";

    public static final String CLOSE_BRACKET = ")";

    public static final String INVERT_CONDITION = "NOT";

    public static final String EQUAL = "==";

    public static final String AND = " AND ";

    public static final String GREATER_THAN = ">=";

    public static final String LESS_THAN = "<=";
    
    public static final String QUOTE = "'";

    public static final String COMMA = ",";

    public static final String NULL_CHECK = "null";

    public static final String START_QUERY = "cast(map:get(propertiesMap,'";

    public static final String REGEX_PREFIX_QUERY = "regex:find(";

    public static final String REGEX_SUFFIX_QUERY = ")";

    public static final String NULL_START_QUERY = "map:get(propertiesMap,'";

    public static final String END_QUERY = "'),'string')";

    public static final String NULL_END_QUERY = "') is null";

    public static final String END_QUERY_LONG = "'),'Long')";

    public static final String END_LONG = "l";

    public static final String PER_USER =  "userLevel";

    public static final String ACROSS_ALL = "apiLevel";
    
    public static final String POLICY_LEVEL_API = "api";

    public static final String POLICY_LEVEL_RESOURCE = "resource";

    public static final String POLICY_LEVEL_SUB = "sub";
    
    public static final String POLICY_LEVEL_GLOBAL = "global";
    
    public static final String POLICY_LEVEL_APP = "app";
    
    public static final String API_THROTTLE_POLICY_TABLE = "AM_API_THROTTLE_POLICY";
    
    public static final String POLICY_GLOBAL_TABLE = "AM_POLICY_GLOBAL";
    
    public static final String POLICY_SUBSCRIPTION_TABLE = "AM_POLICY_SUBSCRIPTION";
    
    public static final String POLICY_APPLICATION_TABLE = "AM_POLICY_APPLICATION";
    
    public static final String POLICY_ID = "POLICY_ID";

    public static final String POLICY_IS_DEPLOYED = "IS_DEPLOYED";

    public static final String MB = "MB";

    public static final String KB = "KB";

    public static final String MEDIATION_NAME_ATTRIBUTE = "name";
    
    public static final String  THROTTLING_TIER_CONTENT_AWARE_SEPERATOR = ">";

}
