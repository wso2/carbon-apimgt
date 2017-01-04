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
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*
*/
package org.wso2.carbon.apimgt.gateway.throttling.utils;

import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
//import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.gateway.throttling.constants.APIThrottleConstants;
import org.wso2.carbon.apimgt.gateway.throttling.dto.AuthenticationContextDTO;
import org.wso2.carbon.messaging.CarbonMessage;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

/**
 * Contains some util methods used by ThrottleCondition Evaluator
 */
public class GatewayUtils {

    private static final Logger log = LoggerFactory.getLogger(GatewayUtils.class);


    /**
     * Extracts the IP from Message Context.
     *
     * @param messageContext Carbon Message Context.
     * @return IP as a String.
     */
    public static String getIp(CarbonMessage messageContext) {

        //Set transport headers of the message
        TreeMap<String, String> transportHeaderMap = (TreeMap<String, String>) messageContext
                .getProperty(APIThrottleConstants.TRANSPORT_HEADERS);
        // Assigning an Empty String so that when doing comparisons, .equals method can be used without explicitly
        // checking for nullity.
        String remoteIP = "";
        //Check whether headers map is null and x forwarded for header is present
        if (transportHeaderMap != null) {
            remoteIP = transportHeaderMap.get(APIThrottleConstants.X_FORWARDED_FOR);
        }

        //Setting IP of the client by looking at x forded for header and  if it's empty get remote address
        if (remoteIP != null && !remoteIP.isEmpty()) {
            if (remoteIP.indexOf(",") > -1) {
                remoteIP = remoteIP.substring(0, remoteIP.indexOf(","));
            }
        } else {
            remoteIP = (String) messageContext.getProperty(APIThrottleConstants.REMOTE_ADDR);
        }

        return remoteIP;
    }

    /**
     * Can be used to extract Query Params from CarbonMessage
     *
     * @param messageContext CarbonMessage
     * @return A Map with Name Value pairs.
     */
    public static Map<String, String> getQueryParams(CarbonMessage messageContext) {
        String queryString = (String) messageContext.getProperty(APIThrottleConstants.REST_URL_POSTFIX);
        if (!StringUtils.isEmpty(queryString)) {
            if (queryString.indexOf("?") > -1) {
                queryString = queryString.substring(queryString.indexOf("?") + 1);
            }
            String[] queryParams = queryString.split("&");
            Map<String, String> queryParamsMap = new HashMap<String, String>();
            String[] queryParamArray;
            String queryParamName, queryParamValue = "";
            for (String queryParam : queryParams) {
                queryParamArray = queryParam.split("=");
                if (queryParamArray.length == 2) {
                    queryParamName = queryParamArray[0];
                    queryParamValue = queryParamArray[1];
                } else {
                    queryParamName = queryParamArray[0];
                }
                queryParamsMap.put(queryParamName, queryParamValue);
            }

            return queryParamsMap;
        }
        return null;
    }

    public static Map getJWTClaims(AuthenticationContextDTO authContext) {
        String[] jwtTokenArray = authContext.getCallerToken().split(Pattern.quote("."));
        // decoding JWT
        try {
            // byte[] jwtByteArray = Base64.decodeBase64(jwtTokenArray[1].getBytes("UTF-8"));
            // TODO: remove below line and uncomment previous use a with suitable decoder
            byte[] jwtByteArray = new byte[jwtTokenArray.length];
            String jwtAssertion = new String(jwtByteArray, "UTF-8");
            JsonParser parser = new JsonParser();
            Object object = parser.parse(jwtAssertion);

            if (object instanceof Map) {
                return (Map) object;
            }
        } catch (UnsupportedEncodingException e) {
            log.error("Error while decoding jwt header", e);
        } catch (JsonSyntaxException e) {
            log.error("Error while parsing jwt header", e);
        }
        return null;
    }

    public static long ipToLong(String ipAddress) {
        long result = 0;
        String[] ipAddressInArray = ipAddress.split("\\.");
        for (int i = 3; i >= 0; i--) {
            long ip = Long.parseLong(ipAddressInArray[3 - i]);
            //left shifting 24,16,8,0 and bitwise OR
            //1. 192 << 24
            //1. 168 << 16
            //1. 1   << 8
            //1. 2   << 0
            result |= ip << (i * 8);

        }
        return result;
    }
}
