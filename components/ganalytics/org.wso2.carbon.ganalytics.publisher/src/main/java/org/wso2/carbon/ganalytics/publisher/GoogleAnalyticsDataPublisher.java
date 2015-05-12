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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for publishing analytics data using the Measurement Protocol. Please refer:
 * https://developers.google.com/analytics/devguides/collection/protocol/v1/devguide
 */
public class GoogleAnalyticsDataPublisher {
    private static final Log log = LogFactory.getLog(GoogleAnalyticsDataPublisher.class.getName());

    /**
     * Use this method to publish using POST.
     * @param payload - use the buildPayload method to retrieve NameValuePair to use as payload here.
     * @param userAgent - set the userAgent - this can be overridden if userAgentOverride (ua) is set in payload
     * @param useSSL - to publish using HTTPS, set this value to true.
     * @return
     */
    public static boolean publishPOST(List<NameValuePair> payload, String userAgent, boolean useSSL) {
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(getURI(useSSL));
        post.setHeader(GoogleAnalyticsConstants.HTTP_HEADER_USER_AGENT, userAgent);

        if (System.getProperty("http.proxyHost") != null && System.getProperty("http.proxyPort") != null)   {
            if (log.isDebugEnabled())   {
                log.debug("Proxy configured, hence routing through configured proxy");
            }
            String proxyHost = System.getProperty("http.proxyHost");
            String proxyPort = System.getProperty("http.proxyPort");
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost(proxyHost, new Integer(proxyPort)));
        }

        try {
            post.setEntity(new UrlEncodedFormEntity(payload));
            HttpResponse response = client.execute(post);

            if((response.getStatusLine().getStatusCode() == 200)
                    && (response.getFirstHeader(GoogleAnalyticsConstants.HTTP_HEADER_CONTENT_TYPE).getValue()
                    .equals(GoogleAnalyticsConstants.RESPONSE_CONTENT_TYPE))) {
                return true;
            }
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Use this method to publish using GET.
     * @param payload - use the buildPayloadString method to retrieve query param string to pass as payload here.
     * @param userAgent - set the userAgent - this can be overridden by using
     * @param useSSL - to publish using HTTPS, set this value to true.
     * @return
     */
    public static boolean publishGET(String payload, String userAgent, boolean useSSL) {
        HttpClient client = new DefaultHttpClient();
        HttpGet get = new HttpGet(getURI(useSSL) + "?" + payload);
        get.setHeader(GoogleAnalyticsConstants.HTTP_HEADER_USER_AGENT, userAgent);

        if (System.getProperty("http.proxyHost") != null && System.getProperty("http.proxyPort") != null)   {
            if (log.isDebugEnabled())   {
                log.debug("Proxy configured, hence routing through configured proxy");
            }
            String proxyHost = System.getProperty("http.proxyHost");
            String proxyPort = System.getProperty("http.proxyPort");
            client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, new HttpHost(proxyHost, new Integer(proxyPort)));
        }

        try {
            HttpResponse response = client.execute(get);
            if((response.getStatusLine().getStatusCode() == 200)
                    && (response.getFirstHeader(GoogleAnalyticsConstants.HTTP_HEADER_CONTENT_TYPE).getValue()
                    .equals(GoogleAnalyticsConstants.RESPONSE_CONTENT_TYPE))) {
                return true;
            }
            return false;
        } catch (ClientProtocolException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * Use this method to retrieve a payload as string.
     * @param o a GoogleAnalyticsData object
     * @return query param string
     */
    public static String buildPayloadString(Object o) {
        String queryString = "";
        Class<?> clazz = o.getClass();

        Field[] fields = clazz.getDeclaredFields();

        for(int i=0; i<fields.length; i++) {
            fields[i].setAccessible(true);
            String fieldValue = getFieldValue(fields[i], o);
            fields[i].setAccessible(false);

            if(fieldValue == null) {
                continue;
            }

            queryString = queryString + encodeString(GoogleAnalyticsConstants.METHOD_TO_PARAM_MAP.get(fields[i].getName()))
                    + "=" + encodeString(fieldValue) + "&";
        }

        queryString = queryString.substring(0, queryString.length()-1);

        return queryString;
    }

    /**
     * Use this method to retireve a payload a list of NameValuePair.
     * @param o a GoogleAnalyticsData object
     * @return list of NamevaluePairs to be used in httpclient entity
     */
    public static List<NameValuePair> buildPayload(Object o) {
        List<NameValuePair> payload = new ArrayList<NameValuePair>();
        Class<?> clazz = o.getClass();

        Field[] fields = clazz.getDeclaredFields();

        for(int i=0; i<fields.length; i++) {
            fields[i].setAccessible(true);
            String fieldValue = getFieldValue(fields[i], o);
            fields[i].setAccessible(false);

            if(fieldValue == null) {
                continue;
            }

            NameValuePair nvp = new BasicNameValuePair(GoogleAnalyticsConstants.METHOD_TO_PARAM_MAP
                    .get(fields[i].getName()), fieldValue);
            payload.add(nvp);
        }

        return payload;
    }

    private static String getFieldValue(Field f, Object o) {
        try {
            Object value = f.get(o);
            if(value instanceof String) {
                return (String) value;
            } else {
                if(value == null) {
                    return null;
                } else {
                    return String.valueOf(value);
                }
            }
        } catch (IllegalAccessException e) {
            log.error("Error while obtaining field value. " + e.getMessage());
        }

        return null;
    }

    private static String encodeString(String queryString) {
        try {
            return URLEncoder.encode(queryString, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            log.warn("Error while percent encoding analytics payload string. Using unencoded string. " + e.getMessage());
        }

        return queryString;
    }

    private static String getURI(boolean useSSL) {
        if(useSSL) {
            return GoogleAnalyticsConstants.HTTPS_ENDPOINT_HOST + GoogleAnalyticsConstants.HTTP_ENDPOINT_URI;
        } else {
            return GoogleAnalyticsConstants.HTTP_ENDPOINT_HOST + GoogleAnalyticsConstants.HTTP_ENDPOINT_URI;
        }
    }

}
