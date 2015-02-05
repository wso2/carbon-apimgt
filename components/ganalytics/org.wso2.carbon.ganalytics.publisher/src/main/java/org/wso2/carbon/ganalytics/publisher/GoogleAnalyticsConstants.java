/*
*  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.ganalytics.publisher;

import java.util.HashMap;
import java.util.Map;

public class GoogleAnalyticsConstants {

    public static final String HTTP_ENDPOINT_HOST   = "http://www.google-analytics.com";
    public static final String HTTP_ENDPOINT_URI    = "/collect";

    public static final String HTTPS_ENDPOINT_HOST  = "https://ssl.google-analytics.com";

    public final static String SESSION_START        = "start";
    public final static String SESSION_END          = "end";

    public final static String HIT_TYPE_PAGEVIEW    = "pageview";
    public final static String HIT_TYPE_SCREENVIEW  = "screenview";
    public final static String HIT_TYPE_EVENT       = "event";
    public final static String HIT_TYPE_TRANSACTION = "transaction";
    public final static String HIT_TYPE_ITEM        = "item";
    public final static String HIT_TYPE_SOCIAL      = "social";
    public final static String HIT_TYPE_EXCEPTION   = "exception";
    public final static String HIT_TYPE_TIMING      = "timing";

    public final static String HTTP_HEADER_USER_AGENT   = "User-Agent";
    public final static String HTTP_HEADER_CONTENT_TYPE = "Content-Type";
    public final static String RESPONSE_CONTENT_TYPE    =  "image/gif";

    public final static Map<String, String> METHOD_TO_PARAM_MAP = new HashMap<String, String>() {{
        /** General **/
        put("protocolVersion", "v");
        put("trackingId", "tid");
        put("anonymizeIp", "aip");
        put("queueTime", "qt");
        put("cacheBuster", "z");

        /** Client **/
        put("clientId", "cid");
        put("userId", "uid");

        /** Session **/
        put("sessionControl", "sc");
        put("IPOverride", "uip");
        put("userAgentOverride", "ua");

        /** Traffic Sources **/
        put("referrer", "dr");
        put("campaignName", "cn");
        put("campaignSource", "cs");
        put("campaignMedium", "cm");
        put("campaignKeyword", "ck");
        put("campaignContent", "cc");
        put("campaignId", "ci");
        put("googleAdwordsId", "gclid");
        put("googleDisplayAdsId", "dclid");

        /** System **/
        put("screenResolutoin", "sr");
        put("viewPortSize", "vp");
        put("documentEncoding", "de");
        put("screenColors", "sd");
        put("userLanguage", "ul");
        put("javaEnabled", "je");
        put("flashVersion", "fl");

        /** Hit **/
        put("hitType", "t");
        put("nonInteractionHit", "ni");

        /** Content Information **/
        put("documentLocationUrl", "dl");
        put("documentHostName", "dh");
        put("documentPath", "dp");
        put("documentTitle", "dt");
        put("screenName", "cd");
        put("linkId", "linkid");

        /** App Tracking **/
        put("appName", "an");
        put("appId", "aid");
        put("appVersion", "av");
        put("appInstallerId", "aiid");

        /** Event Tracking **/
        put("eventCategory", "ec");
        put("eventAction", "ea");
        put("eventLabel", "el");
        put("eventValue","ev");

        /** E-Commerce **/
        put("transactionId", "ti");
        put("transactionAffiliation","ta");
        put("transactionRevenue", "tr");
        put("transactionShipping", "ts");
        put("transactionTax", "tt");
        put("itemName", "in");
        put("itemPrice", "ip");
        put("itemQty", "iq");
        put("itemCode","ic");
        put("itemCategory", "iv");
        put("currencyCode", "cu");

        /** Social Interactions **/
        put("socialNetwork", "sn");
        put("socialAction", "sa");
        put("socialActionTarget", "st");

        /** Timing **/
        put("userTimingCategory", "utc");
        put("userTimingVariableName", "utv");
        put("userTimingTime", "utt");
        put("userTimingLabel", "utl");
        put("pageLoadTime", "plt");
        put("dnsTime", "dns");
        put("pageDownloadTime", "pdt");
        put("redirectResponseTime", "rrt");
        put("tcpConnectTime", "tcp");
        put("serverResponseTime", "srt");

        /** Exceptions **/
        put("exceptionDescription", "exd");
        put("fatalException", "exf");

        /** Custom Dimensions **/
        /** TODO Need to fix below to include multi dimensions simply applying a value to below will not work **/
        put("customDimension","cd");
        put("customMetric", "cm");

        /** Content Experiments **/
        put("experimentId", "xid");
        put("experimentVariant", "xvar");
    }};
}
