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

package org.wso2.carbon.apimgt.gateway.handlers.analytics;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.Constants;
import org.apache.axis2.util.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.Entry;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.MethodStats;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.utils.APIMgtGoogleAnalyticsUtils;
import org.wso2.carbon.apimgt.tracing.TracingSpan;
import org.wso2.carbon.apimgt.tracing.TracingTracer;
import org.wso2.carbon.apimgt.tracing.Util;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetrySpan;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetryTracer;
import org.wso2.carbon.apimgt.tracing.telemetry.TelemetryUtil;
import org.wso2.carbon.ganalytics.publisher.GoogleAnalyticsConstants;
import org.wso2.carbon.ganalytics.publisher.GoogleAnalyticsData;
import org.wso2.carbon.ganalytics.publisher.GoogleAnalyticsDataPublisher;
import org.wso2.carbon.ganalytics.publisher.ga4.GoogleAnalytics4Constants;
import org.wso2.carbon.ganalytics.publisher.ga4.GoogleAnalytics4Data;
import org.wso2.carbon.ganalytics.publisher.ga4.GoogleAnalytics4DataPublisher;
import org.wso2.carbon.ganalytics.publisher.ga4.event.PageViewEvent;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.xml.namespace.QName;

public class APIMgtGoogleAnalyticsTrackingHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(APIMgtGoogleAnalyticsTrackingHandler.class);

	private static final String GOOGLE_ANALYTICS_TRACKER_VERSION = "1";

	private static final String COOKIE_NAME = "__utmmobile";

	private static final String ANONYMOUS_USER_ID = "anonymous";

    private static final String USER_IP = "user_ip";
	
	/** The key for getting the google analytics configuration - key refers to a/an [registry] entry    */
    private String configKey = null;
    /** Version number of the throttle policy */
    private long version;

    protected GoogleAnalyticsConfig config = null;

    @MethodStats
    @Override
    public boolean handleRequest(MessageContext msgCtx) {

        TracingSpan tracingSpan = null;
        TracingTracer tracingTracer = null;
        TelemetrySpan span = null;
        TelemetryTracer tracer = null;
        Map<String, String> tracerSpecificCarrier = new HashMap<>();
        if (TelemetryUtil.telemetryEnabled()) {
            TelemetrySpan responseLatencySpan =
                    (TelemetrySpan) msgCtx.getProperty(APIMgtGatewayConstants.RESOURCE_SPAN);
            tracer = ServiceReferenceHolder.getInstance().getTelemetryTracer();
            span = TelemetryUtil.startSpan(APIMgtGatewayConstants.GOOGLE_ANALYTICS_HANDLER, responseLatencySpan,
                    tracer);
        } else if (Util.tracingEnabled()) {
            TracingSpan responseLatencySpan =
                    (TracingSpan) msgCtx.getProperty(APIMgtGatewayConstants.RESOURCE_SPAN);
            tracingTracer = Util.getGlobalTracer();
            tracingSpan = Util.startSpan(APIMgtGatewayConstants.GOOGLE_ANALYTICS_HANDLER, responseLatencySpan,
                    tracingTracer);
        }
        try {
            if (configKey == null) {
                throw new SynapseException("Google Analytics configuration unspecified for the API");
            }

            Entry entry = msgCtx.getConfiguration().getEntryDefinition(configKey);
            if (entry == null) {
                log.warn("Cannot find Google Analytics configuration using key: " + configKey);
                return true;
            }
            Object entryValue = null;
            boolean reCreate = false;

            if (entry.isDynamic()) {
                if ((!entry.isCached()) || (entry.isExpired()) || config == null) {
                    entryValue = msgCtx.getEntry(this.configKey);
                    if (this.version != entry.getVersion()) {
                        reCreate = true;
                    }
                }
            } else if (config == null) {
                entryValue = msgCtx.getEntry(this.configKey);
            }

            if (reCreate || config == null) {
                if (entryValue == null || !(entryValue instanceof OMElement)) {
                    log.warn("Unable to load Google Analytics configuration using key: " + configKey);
                    return true;
                }
                version = entry.getVersion();
                config = getGoogleAnalyticsConfig((OMElement) entryValue);
            }

            if (config == null) {
                log.warn("Unable to create Google Analytics configuration using key: " + configKey);
                return true;
            }
            if (!config.isEnabled()) {
                return true;
            }
            try {
                if (TelemetryUtil.telemetryEnabled() || Util.tracingEnabled()) {
                    if (Util.tracingEnabled()) {
                        Util.inject(tracingSpan, tracingTracer, tracerSpecificCarrier);
                    } else {
                        TelemetryUtil.inject(span, tracerSpecificCarrier);
                    }
                    if (org.apache.axis2.context.MessageContext.getCurrentMessageContext() != null) {
                        Map headers =
                                (Map) org.apache.axis2.context.MessageContext.getCurrentMessageContext().getProperty(
                                        org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
                        headers.putAll(tracerSpecificCarrier);
                        org.apache.axis2.context.MessageContext.getCurrentMessageContext()
                                .setProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS, headers);
                    }
                }
                trackPageView(msgCtx);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            return true;
        } catch (Exception e) {
            if (TelemetryUtil.telemetryEnabled()) {
                if (Util.tracingEnabled() && tracingSpan != null) {
                    Util.setTag(tracingSpan, APIMgtGatewayConstants.ERROR,
                            APIMgtGatewayConstants.GOOGLE_ANALYTICS_ERROR);
                } else if (!Util.tracingEnabled() && span != null) {
                    TelemetryUtil.setTag(span, APIMgtGatewayConstants.ERROR,
                            APIMgtGatewayConstants.GOOGLE_ANALYTICS_ERROR);
                }
            }
            throw e;
        } finally {
            if (TelemetryUtil.telemetryEnabled()) {
                TelemetryUtil.finishSpan(span);
            } else if (Util.tracingEnabled()) {
                Util.finishSpan(tracingSpan);
            }
        }
    }

    protected GoogleAnalyticsConfig getGoogleAnalyticsConfig(OMElement entryValue) {
        return new GoogleAnalyticsConfig(entryValue);
    }

    /**
     * Track a page view, updates all the cookies and campaign tracker, makes a
     * server side request to Google Analytics and writes the transparent gif
     * byte data to the response.
     *
     * @throws Exception
     */
    private void trackPageView(MessageContext msgCtx) throws Exception {
        @SuppressWarnings("rawtypes")
        Map headers = (Map) ((Axis2MessageContext) msgCtx).getAxis2MessageContext()
                                               .getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

        String host = (String) headers.get(HttpHeaders.HOST);
        String domainName = host;
        if (host != null && host.indexOf(":") != -1) {
            domainName = host.substring(0, host.indexOf(":"));
        }
        if (isEmpty(domainName)) {
            domainName = "";
        }

        // Get client IP
        String xForwardedFor = (String) headers
                .get(org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants.X_FORWARDED_FOR_HEADER);
        String userIP;
        if(xForwardedFor == null || xForwardedFor.isEmpty()) {
            userIP = (String) ((Axis2MessageContext) msgCtx).getAxis2MessageContext()
                    .getProperty(org.apache.axis2.context.MessageContext.REMOTE_ADDR);
        } else {
            userIP = xForwardedFor.split(",")[0];
        }
        String path = (String) msgCtx.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
        String documentPath = path;
        if (isEmpty(documentPath)) {
            documentPath = "";
        }

        String userAgent = (String) headers.get(HttpHeaders.USER_AGENT);
        if (isEmpty(userAgent)) {
            userAgent = "";
        }

        String httpMethod = (String) ((Axis2MessageContext) msgCtx).getAxis2MessageContext()
                .getProperty(Constants.Configuration.HTTP_METHOD);

        String universalAnalyticsTrackingId = config.googleAnalyticsTrackingID;
        if (universalAnalyticsTrackingId != null) {
            trackPageViewWithUniversalAnalytics(universalAnalyticsTrackingId, documentPath, domainName, userIP,
                    userAgent, httpMethod, msgCtx);
        }

        String googleAnalytics4MeasurementId = config.googleAnalyticsMeasurementID;
        String googleAnalytics4ApiSecret = config.apiSecret;
        if (googleAnalytics4MeasurementId != null && googleAnalytics4ApiSecret != null) {
            trackPageViewWithGoogleAnalytics4(googleAnalytics4ApiSecret, googleAnalytics4MeasurementId, documentPath,
                    host, userIP, userAgent, httpMethod, msgCtx);
        }
    }

    private void trackPageViewWithUniversalAnalytics(String trackingId, String documentPath, String domainName,
                                                     String userIP, String userAgent, String httpMethod,
                                                     MessageContext msgCtx)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {

        String visitorId = getVisitorId(trackingId, userAgent, msgCtx);
        /* Set the visitorId in MessageContext */
        msgCtx.setProperty(COOKIE_NAME, visitorId);

        GoogleAnalyticsData data = new GoogleAnalyticsData
                .DataBuilder(trackingId, GOOGLE_ANALYTICS_TRACKER_VERSION, visitorId,
                GoogleAnalyticsConstants.HIT_TYPE_PAGEVIEW)
                .setDocumentPath(documentPath)
                .setDocumentHostName(domainName)
                .setDocumentTitle(httpMethod)
                .setSessionControl("end")
                .setCacheBuster(APIMgtGoogleAnalyticsUtils.getCacheBusterId())
                .setIPOverride(userIP)
                .build();

        String payload = GoogleAnalyticsDataPublisher.buildPayloadString(data);
        if (log.isDebugEnabled()) {
            log.debug("Publishing https GET from gateway to Google analytics in UA format with ID: "
                    + msgCtx.getMessageID() + " started at "
                    + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
        }
        GoogleAnalyticsDataPublisher.publishGET(payload, userAgent, false);
        if (log.isDebugEnabled()) {
            log.debug("Publishing https GET from gateway to Google analytics in UA format with ID: "
                    + msgCtx.getMessageID() + " ended at "
                    + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
        }
    }

    private void trackPageViewWithGoogleAnalytics4(String apiSecret, String measurementId, String documentPath,
                                                   String host, String userIP, String userAgent, String httpMethod,
                                                   MessageContext msgCtx)
            throws UnsupportedEncodingException, NoSuchAlgorithmException {

        String visitorId = getVisitorId(measurementId, userAgent, msgCtx);

        /* Set the visitorId in MessageContext */
        msgCtx.setProperty(COOKIE_NAME, visitorId);

        String pageTitle = constructPageTitleForRequestPath(httpMethod, documentPath);
        String pageLocation = host + documentPath;

        PageViewEvent pageViewEvent = new PageViewEvent();
        pageViewEvent.setPageTitle(pageTitle);
        pageViewEvent.setPageLocation(pageLocation);
        pageViewEvent.setUserAgent(userAgent);
        pageViewEvent.putParam(USER_IP, userIP);
        pageViewEvent.putParam(GoogleAnalytics4Constants.ENGAGEMENT_TIME_MSEC_PARAM, "1");

        GoogleAnalytics4Data data = new GoogleAnalytics4Data(apiSecret, measurementId);
        data.setClientId(visitorId);
        data.addEvent(pageViewEvent);

        if (log.isDebugEnabled()) {
            log.debug("Publishing https POST from gateway to Google analytics in GA4 format with ID: "
                    + msgCtx.getMessageID() + " started at "
                    + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
        }
        boolean status = GoogleAnalytics4DataPublisher.publishData(data, userAgent);
        if (log.isDebugEnabled()) {
            log.debug("Publishing https POST from gateway to Google analytics in GA4 format with ID: "
                    + msgCtx.getMessageID() + " ended " + (status ? "successfully at " : "with failure at ")
                    + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
        }
    }

    private static String constructPageTitleForRequestPath(String httpMethod, String requestPath) {

        return httpMethod + " " + requestPath;
    }

    /**
     * A string is empty in our terms, if it is null, empty or a dash.
     */
    private static boolean isEmpty(String in) {

        return in == null || "-".equals(in) || "".equals(in);
    }

    /**
     * Generate a visitor id for this hit. If there is a visitor id in the
     * messageContext, use that. Otherwise use a random number.
     */
    private static String getVisitorId(String account, String userAgent, MessageContext msgCtx)
            throws NoSuchAlgorithmException, UnsupportedEncodingException {

        if (msgCtx.getProperty(COOKIE_NAME) != null) {
            return (String) msgCtx.getProperty(COOKIE_NAME);
        }
        String message;

        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(msgCtx);
        if (authContext != null) {
            message = authContext.getApiKey();
        } else {
            message = ANONYMOUS_USER_ID;
        }

        MessageDigest m = MessageDigest.getInstance("MD5");
        m.update(message.getBytes("UTF-8"), 0, message.length());
        byte[] sum = m.digest();
        BigInteger messageAsNumber = new BigInteger(1, sum);
        String md5String = messageAsNumber.toString(16);

        /* Pad to make sure id is 32 characters long. */
        while (md5String.length() < 32) {
            md5String = "0" + md5String;
        }

        return "0x" + md5String.substring(0, 16);
    }

    @MethodStats
	@Override
	public boolean handleResponse(MessageContext arg0) {
        return true;
	}
	
	private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }
	
    private class GoogleAnalyticsConfig {

        private boolean enabled;
        private String googleAnalyticsTrackingID;
        private String googleAnalyticsMeasurementID;
        private String apiSecret;

        public GoogleAnalyticsConfig(OMElement config) {

            googleAnalyticsTrackingID =
                    getConfigPropertyValue(org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants.API_GOOGLE_ANALYTICS_TRACKING_ID, config);
            googleAnalyticsMeasurementID =
                    getConfigPropertyValue(org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants.API_GOOGLE_ANALYTICS_MEASUREMENT_ID, config);
            apiSecret =
                    getConfigPropertyValue(org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants.API_GOOGLE_ANALYTICS_API_SECRET, config);
            String googleAnalyticsEnabledStr =
                    getConfigPropertyValue(org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants.API_GOOGLE_ANALYTICS_TRACKING_ENABLED, config);
            enabled = googleAnalyticsEnabledStr != null && JavaUtils.isTrueExplicitly(googleAnalyticsEnabledStr);
        }

        private String getConfigPropertyValue(String propertyName, OMElement config) {

            OMElement omElement = config.getFirstChildWithName(new QName(propertyName));
            if (omElement != null) {
                return omElement.getText();
            }
            return null;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isEnabled() {
            return enabled;
        }
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }

}
