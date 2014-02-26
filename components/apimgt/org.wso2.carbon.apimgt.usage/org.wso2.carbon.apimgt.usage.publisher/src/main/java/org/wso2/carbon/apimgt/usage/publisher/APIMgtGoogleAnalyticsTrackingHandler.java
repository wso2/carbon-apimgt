package org.wso2.carbon.apimgt.usage.publisher;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpHeaders;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.usage.publisher.internal.UsageComponent;

public class APIMgtGoogleAnalyticsTrackingHandler extends AbstractHandler {

	private static final Log log = LogFactory
			.getLog(APIMgtGoogleAnalyticsTrackingHandler.class);

	private static final String GOOGLE_ANALYTICS_TRACKER_VERSION = "4.4sj";

	private static final String COOKIE_NAME = "__utmmobile";

	private static final String UTM_GIF_LOCATION = "http://www.google-analytics.com/__utm.gif";

	private boolean enabled = UsageComponent.getApiMgtConfigReaderService().isGoogleAnalyticsTrackingEnabled();

	private String googleAnalytictPropertyID = UsageComponent.getApiMgtConfigReaderService().
			getGoogleAnalyticsTrackingID();

	private ExecutorService executor = Executors.newFixedThreadPool(1);

	@Override
	public boolean handleRequest(MessageContext msgCtx) {
		if (!enabled) {
			return true;
		}
		try {
			trackPageView(msgCtx);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return true;
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
		Map headers = (Map) ((Axis2MessageContext) msgCtx).getAxis2MessageContext().
			getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);

		String host = (String) headers.get(HttpHeaders.HOST);
		String domainName = host;
		if (host != null && host.indexOf(":") != -1){
			domainName = host.substring(0, host.indexOf(":"));
		}
		if (isEmpty(domainName)) {
			domainName = "";
		}

		String documentReferer = "-";

		String path = (String) msgCtx
				.getProperty(RESTConstants.REST_FULL_REQUEST_PATH);
		String documentPath = path;
		if (isEmpty(documentPath)) {
			documentPath = "";
		} else {
			documentPath = URLDecoder.decode(documentPath, "UTF-8");
		}

		String account = googleAnalytictPropertyID;

		String userAgent = (String) headers.get(HttpHeaders.USER_AGENT);
		if (isEmpty(userAgent)) {
			userAgent = "";
		}

		String visitorId = getVisitorId(account, userAgent, msgCtx);

		/* Set the visitorId in MessageContext */
		msgCtx.setProperty(COOKIE_NAME, visitorId);

		/* Construct the gif hit url */
		String utmUrl = UTM_GIF_LOCATION + "?" + "utmwv="
				+ GOOGLE_ANALYTICS_TRACKER_VERSION + "&utmn="
				+ getRandomNumber() + "&utmhn=" + "none" + "&utmr="
				+ URLEncoder.encode(documentReferer, "UTF-8") + "&utmp="
				+ URLEncoder.encode(documentPath, "UTF-8") + "&utmac="
				+ account + "&utmcc=__utma%3D999.999.999.999.999.1%3B"
				+ "&utmvid=" + visitorId + "&utmip=" + "";

		Runnable googleAnalyticsPublisher = new GoogleAnalyticsPublisher(utmUrl, userAgent);
		executor.execute(googleAnalyticsPublisher);
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
	private static String getVisitorId(String account, String userAgent,MessageContext msgCtx) 
			throws NoSuchAlgorithmException, UnsupportedEncodingException {

		if (msgCtx.getProperty(COOKIE_NAME) != null) {
			return (String) msgCtx.getProperty(COOKIE_NAME);
		}

		String message;
		message = userAgent + getRandomNumber() + UUID.randomUUID().toString();

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

	/**
	 * Get a random number string.
	 * 
	 * @return
	 */
	private static String getRandomNumber() {
		return Integer.toString((int) (Math.random() * 0x7fffffff));
	}

	/**
	 * Make a tracking request to Google Analytics from this server.
	 * 
	 */
	private class GoogleAnalyticsPublisher implements Runnable {
		String utmUrl;
		String userAgent;

		public GoogleAnalyticsPublisher(String utmUrl, String userAgent) {
			this.utmUrl = utmUrl;
			this.userAgent = userAgent;
		}

		@Override
		public void run() {
			try {
				URL url = new URL(utmUrl);
				URLConnection connection = url.openConnection();
				connection.setUseCaches(false);
				connection.addRequestProperty("User-Agent", userAgent);
				connection.getContent();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}

	}

	@Override
	public boolean handleResponse(MessageContext arg0) {
		return true;
	}

}
