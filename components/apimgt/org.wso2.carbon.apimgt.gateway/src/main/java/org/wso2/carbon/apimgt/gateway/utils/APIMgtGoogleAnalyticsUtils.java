/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.ganalytics.publisher.GoogleAnalyticsConstants;
import org.wso2.carbon.ganalytics.publisher.GoogleAnalyticsData;
import org.wso2.carbon.ganalytics.publisher.GoogleAnalyticsDataPublisher;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

public class APIMgtGoogleAnalyticsUtils {
    private static final Log log = LogFactory.getLog(APIMgtGoogleAnalyticsUtils.class);
    private static final String ANONYMOUS_USER_ID = "anonymous";
    private static final String GOOGLE_ANALYTICS_TRACKER_VERSION = "1";
    private String configKey = null;
    private GoogleAnalyticsConfig gaConfig = null;

    private class GoogleAnalyticsConfig {
        private boolean enabled;
        private String googleAnalyticsTrackingID;

        public GoogleAnalyticsConfig(OMElement config) {
            googleAnalyticsTrackingID = config
                    .getFirstChildWithName(new QName(Constants.API_GOOGLE_ANALYTICS_TRACKING_ID)).getText();
            String googleAnalyticsEnabledStr = config
                    .getFirstChildWithName(new QName(Constants.API_GOOGLE_ANALYTICS_TRACKING_ENABLED)).getText();
            enabled = googleAnalyticsEnabledStr != null && JavaUtils.isTrueExplicitly(googleAnalyticsEnabledStr);
        }
    }

    /**
     * Initialize the google analytics publisher by reading tenants google analytics
     * configuration from the registry
     *
     * @param tenantDomain Tenant domain of the current tenant
     */
    public void init(String tenantDomain) {

        String googleAnalyticsConfig = DataHolder.getInstance().getGoogleAnalyticsConfig(tenantDomain);
        if (StringUtils.isNotEmpty(googleAnalyticsConfig)) {
            try (InputStream in = new ByteArrayInputStream(googleAnalyticsConfig.getBytes())) {
                StAXOMBuilder builder = new StAXOMBuilder(in);
                this.gaConfig = new GoogleAnalyticsConfig(builder.getDocumentElement());
            } catch (XMLStreamException | IOException e) {
                // flow should not break. Therefore ignoring the exception
                log.error("Failed to retrieve google analytics configurations for tenant:" + tenantDomain);
            }
        } else {
            log.error("Failed to retrieve google analytics configurations for tenant:" + tenantDomain);
        }
    }

    /**
     * Initialize the google analytics publisher using provided xml configurations
     *
     * @param config Google Analytics configuration element
     */
    public void init(OMElement config) {
        this.gaConfig = new GoogleAnalyticsConfig(config);
    }

    /**
     * Generates a 32 character length random number for cacheBusterId
     *
     * @return cacheBusterId
     * @throws NoSuchAlgorithmException
     * @throws UnsupportedEncodingException
     */
    public static String getCacheBusterId() throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String message = getRandomNumber() + UUID.randomUUID().toString();
        MessageDigest m = MessageDigest.getInstance("MD5");
        m.update(message.getBytes("UTF-8"), 0, message.length());
        byte[] sum = m.digest();
        BigInteger messageAsNumber = new BigInteger(1, sum);
        String md5String = messageAsNumber.toString(16);

        // Pad to make sure id is 32 characters long.
        while (md5String.length() < 32) {
            md5String = "0" + md5String;
        }
        return "0x" + md5String.substring(0, 16);
    }

    /**
     * Generate a random number
     *
     * @return random number
     */
    private static String getRandomNumber() {
        return Integer.toString((int) (Math.random() * 0x7fffffff));
    }

    /**
     * Generate a visitor id for this hit. If there is a visitor id in the
     * messageContext, use that. Otherwise use a random number.
     *
     * @param authHeader Authentication header of the request
     */
    private String getVisitorId(String authHeader) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        String message = authHeader.split(" ")[1];
        if (message == null) {
            message = ANONYMOUS_USER_ID;
        }

        MessageDigest m = MessageDigest.getInstance("MD5");
        m.update(message.getBytes("UTF-8"), 0, message.length());
        byte[] sum = m.digest();
        BigInteger messageAsNumber = new BigInteger(1, sum);
        String md5String = messageAsNumber.toString(16);

        // Pad to make sure id is 32 characters long.
        while (md5String.length() < 32) {
            md5String = "0" + md5String;
        }

        return "0x" + md5String.substring(0, 16);
    }

    /**
     * Publish page tracking data to google analytics
     *
     * @param analyticsData Attributes required to be sent to Google Analytics
     * @param userAgent User-Agent of the client who sent the request
     * @param authHeader authentication header value of the request
     */
    public void publishGATrackingData(GoogleAnalyticsData.DataBuilder analyticsData, String userAgent,
            String authHeader) {
        try {
            if (gaConfig == null || !gaConfig.enabled) {
                return;
            }
            String clientId = getVisitorId(authHeader);
            GoogleAnalyticsData data = analyticsData
                    .setProtocolVersion(GOOGLE_ANALYTICS_TRACKER_VERSION)
                    .setTrackingId(gaConfig.googleAnalyticsTrackingID)
                    .setClientId(clientId)
                    .setHitType(GoogleAnalyticsConstants.HIT_TYPE_PAGEVIEW)
                    .build();

            String payload = GoogleAnalyticsDataPublisher.buildPayloadString(data);
            GoogleAnalyticsDataPublisher.publishGET(payload, userAgent, false);
        } catch (Exception e) {
            // flow should not break if event publishing failed. Therefore catching generic Exception and ignoring
            log.error("Cannot publish event. " + e.getMessage(), e);
        }
    }
}
