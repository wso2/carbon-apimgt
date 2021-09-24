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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

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
import org.wso2.carbon.apimgt.gateway.utils.APIMgtGoogleAnalyticsUtils;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.tracing.TracingSpan;
import org.wso2.carbon.apimgt.tracing.TracingTracer;
import org.wso2.carbon.apimgt.tracing.Util;
import org.wso2.carbon.ganalytics.publisher.GoogleAnalyticsConstants;
import org.wso2.carbon.ganalytics.publisher.GoogleAnalyticsData;
import org.wso2.carbon.ganalytics.publisher.GoogleAnalyticsDataPublisher;

public class APIMgtGoogleAnalyticsTrackingHandler extends AbstractHandler {

    private static final Log log = LogFactory.getLog(APIMgtGoogleAnalyticsTrackingHandler.class);

	private static final String GOOGLE_ANALYTICS_TRACKER_VERSION = "1";

	private static final String COOKIE_NAME = "__utmmobile";

	private static final String ANONYMOUS_USER_ID = "anonymous";
	
	/** The key for getting the google analytics configuration - key refers to a/an [registry] entry    */
    private String configKey = null;
    /** Version number of the throttle policy */
    private long version;

    protected GoogleAnalyticsConfig config = null;

    @MethodStats
    @Override
    public boolean handleRequest(MessageContext msgCtx) {

        TracingSpan span = null;
        TracingTracer tracer = null;
        Map<String, String> tracerSpecificCarrier = new HashMap<>();
        if (Util.tracingEnabled()) {
            TracingSpan responseLatencySpan = (TracingSpan) msgCtx.getProperty(APIMgtGatewayConstants.RESOURCE_SPAN);
            tracer = Util.getGlobalTracer();
            span = Util.startSpan(APIMgtGatewayConstants.GOOGLE_ANALYTICS_HANDLER, responseLatencySpan, tracer);
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
                if (Util.tracingEnabled()) {
                    Util.inject(span, tracer, tracerSpecificCarrier);
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
            if (Util.tracingEnabled() && span != null) {
                Util.setTag(span, APIMgtGatewayConstants.ERROR, APIMgtGatewayConstants.GOOGLE_ANALYTICS_ERROR);
            }
            throw e;
        } finally {
            if (Util.tracingEnabled()) {
                Util.finishSpan(span);
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

        String account = config.googleAnalyticsTrackingID;

        String userAgent = (String) headers.get(HttpHeaders.USER_AGENT);
        if (isEmpty(userAgent)) {
            userAgent = "";
        }

        String visitorId = getVisitorId(account, userAgent, msgCtx);

        /* Set the visitorId in MessageContext */
        msgCtx.setProperty(COOKIE_NAME, visitorId);

        String httpMethod =
                            (String) ((Axis2MessageContext) msgCtx).getAxis2MessageContext()
                                                                   .getProperty(Constants.Configuration.HTTP_METHOD);

		GoogleAnalyticsData data = new GoogleAnalyticsData
                .DataBuilder(account, GOOGLE_ANALYTICS_TRACKER_VERSION , visitorId , GoogleAnalyticsConstants.HIT_TYPE_PAGEVIEW)
                .setDocumentPath(documentPath)
                .setDocumentHostName(domainName)
                .setDocumentTitle(httpMethod)
                .setSessionControl("end")
                .setCacheBuster(APIMgtGoogleAnalyticsUtils.getCacheBusterId())
                .setIPOverride(userIP)
                .build();

        String payload = GoogleAnalyticsDataPublisher.buildPayloadString(data);
        if (log.isDebugEnabled()) {
            log.debug("Publishing https GET from gateway to Google analytics" + " with ID: " + msgCtx.getMessageID()
                    + " started at " + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
        }
        GoogleAnalyticsDataPublisher.publishGET(payload, userAgent, false);
        if (log.isDebugEnabled()) {
            log.debug("Publishing https GET from gateway to Google analytics" + " with ID: " + msgCtx.getMessageID()
                    + " ended at " + new SimpleDateFormat("[yyyy.MM.dd HH:mm:ss,SSS zzz]").format(new Date()));
        }
	}

	/**
	 * A string is empty in our terms, if it is null, empty or a dash.
	 */
	private static boolean isEmpty(String in) {
		return in == null || "-".equals(in) || "".equals(in);
	}

	/**
	 * 
	 * Generate a visitor id for this hit. If there is a visitor id in the
	 * messageContext, use that. Otherwise use a random number.
	 * 
	 */
	private static String getVisitorId(String account, String userAgent, MessageContext msgCtx) 
			throws NoSuchAlgorithmException, UnsupportedEncodingException {

		if (msgCtx.getProperty(COOKIE_NAME) != null) {
			return (String) msgCtx.getProperty(COOKIE_NAME);
		}
		String message;
		
		AuthenticationContext authContext  = APISecurityUtils.getAuthenticationContext(msgCtx);
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
		
		public GoogleAnalyticsConfig(OMElement config) {
            googleAnalyticsTrackingID = config.getFirstChildWithName(new QName(
                    org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants.API_GOOGLE_ANALYTICS_TRACKING_ID))
                    .getText();
            String googleAnalyticsEnabledStr = config.getFirstChildWithName(new QName(
                    org.wso2.carbon.apimgt.gateway.handlers.analytics.Constants.API_GOOGLE_ANALYTICS_TRACKING_ENABLED))
                    .getText();
            enabled =  googleAnalyticsEnabledStr != null && JavaUtils.isTrueExplicitly(googleAnalyticsEnabledStr);
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
