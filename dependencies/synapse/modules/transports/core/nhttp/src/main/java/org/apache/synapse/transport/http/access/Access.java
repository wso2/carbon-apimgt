/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.synapse.transport.http.access;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Header;
import org.apache.http.HttpMessage;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.params.HttpParams;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * The class to handle the HTTP Access Logs, patterns and the major functionality.
 * Major Code segment borrowed from Apache Tomcat's
 * org.apache.catalina.valves.AccessLogValve with thanks.
 */
public class Access {
    private static Log log = LogFactory.getLog(Access.class);

    /**
     * Array of AccessLogElement, they will be used to make log message.
     */
    protected AccessLogElement[] logElements = null;

    protected String pattern = AccessConstants.LOG_PATTERN;

    private static AccessLogger accessLogger;

    private static ConcurrentLinkedQueue<HttpRequest> requestQueue;
    private static ConcurrentLinkedQueue<HttpResponse> responseQueue;

    private static final int LOG_FREQUENCY_IN_SECONDS = 30;

    private Date date;

    /**
     * Constructor of AccessLog. AccessHandler has a static object of Access.
     *
     * @param log          - Log passed as a param. Default is Log of the same class.
     * @param accessLogger - AccessLogger Object
     */
    public Access(final Log log, AccessLogger accessLogger) {
        super();
        Access.log = log;
        Access.accessLogger = accessLogger;
        requestQueue = new ConcurrentLinkedQueue<HttpRequest>();
        responseQueue = new ConcurrentLinkedQueue<HttpResponse>();
        logElements = createLogElements();
        logAccesses();
    }

    /**
     * Adds the accesses to the queue.
     *
     * @param request - HttpRequest
     */
    public void addAccessToQueue(HttpRequest request) {
        requestQueue.add(request);
    }

    /**
     * Adds the accesses to the queue.
     *
     * @param response - HttpResponse
     */
    public void addAccessToQueue(HttpResponse response) {
        responseQueue.add(response);
    }

    /**
     * logs the request and response accesses.
     */
    public void logAccesses() {
        TimerTask logRequests = new LogRequests();
        TimerTask logResponses = new LogResponses();
        Timer requestTimer = new Timer();
        Timer responseTimer = new Timer();
        // Retry in 30 seconds
        long retryIn = 1000 * LOG_FREQUENCY_IN_SECONDS;
        requestTimer.schedule(logRequests, 0, retryIn);
        responseTimer.schedule(logResponses, 0, retryIn);
    }

    private class LogRequests extends TimerTask {
        public void run() {
            while (!requestQueue.isEmpty()) {
                HttpRequest req = requestQueue.poll();
                log(req, null);
            }
        }
    }

    private class LogResponses extends TimerTask {
        public void run() {
            while (!responseQueue.isEmpty()) {
                HttpResponse res = responseQueue.poll();
                log(null, res);
            }
        }
    }

    /**
     * The log method that is called from the NHttpClient and Server connection classes.
     *
     * @param request  - HttpRequest
     * @param response - HttpResponse
     */
    public void log(HttpRequest request, HttpResponse response) {
        date = AccessTimeUtil.getDate();
        //String commonLogFormatDate = AccessTimeUtil.getAccessDate(date);

        StringBuilder result = new StringBuilder(128);

        for (AccessLogElement logElement : logElements) {
            logElement.addElement(result, date, request, response);
        }
        String logString = result.toString();
        log.debug(logString);      //log to the console
        accessLogger.log(logString);      //log to the file
    }

    /**
     * gets the header values from the given message, with the given name.
     *
     * @param message - The message, HttpRequest or HttpResponse
     * @param name    - The header, which we need to get the value of.
     * @return - The header value.
     */
    protected static String getHeaderValues(HttpMessage message, String name) {
        int length = 0;
        Header[] header = new Header[0];
        StringBuffer headerValue = new StringBuffer();
        try {
            header = message.getHeaders(name);
            length = header.length;
        } catch (Exception e) {
            // The header doesn't exist
        }
        if (length == 0) {
            return "-";
        } else if (length == 1) {
            return header[0].getValue();
        } else {
            headerValue.append(header[0].getValue());
            for (int i = 1; i < length; i++) {
                headerValue.append(" - ").append(header[i].getValue());
            }
        }
        return headerValue.toString();
    }

    protected static String getParam(HttpMessage message, String paramName) {
        HttpParams params = message.getParams();
        String param = (String) params.getParameter(paramName);
        if (param == null) {
            param = "-";
        }

        return param;
    }

    public static String getHostElement(HttpMessage message) {
        return getHeaderValues(message, "Host"); //%h;
    }

    public static String getLogicalUserNameElement(HttpMessage message) {
        if (message != null) {  //%l
            return "-";
        }
        return "";
    }

    public static String getUserNameElement(HttpMessage message) {
        return getHeaderValues(message, "From");     //%u
    }

    public static String getCookieElement(HttpMessage message) {
        return getHeaderValues(message, "Cookie");      // %c
    }

    public static String getRefererElement(HttpMessage message) {
        return getHeaderValues(message, "Referer"); //%{Referer}i;           %f
    }

    public static String getUserAgentElement(HttpMessage message) {
        return getHeaderValues(message, "User-Agent");           //%{User-Agent} %a
    }

    public static String getAcceptElement(HttpMessage message) {
        return getHeaderValues(message, "Accept");
    }

    public static String getAcceptLanguageElement(HttpMessage message) {
        return getHeaderValues(message, "Accept-Language");
    }

    public static String getAcceptEncodingElement(HttpMessage message) {
        return getHeaderValues(message, "Accept-Encoding");
    }

    public static String getAcceptCharSetElement(HttpMessage message) {
        return getHeaderValues(message, "Accept-Charset");
    }

    public static String getConnectionElement(HttpMessage message) {
        return getHeaderValues(message, "Connection");       //Keep-Alive
    }

    public static String getContentTypeElement(HttpMessage message) {
        return getHeaderValues(message, "Content-Type");
    }

    public static String getKeepAliveElement(HttpMessage message) {
        return getHeaderValues(message, "Keep-Alive");
    }

    public static String getTransferEncodingElement(HttpMessage message) {
        return getHeaderValues(message, "Transfer-Encoding");
    }

    public static String getContentEncodingElement(HttpMessage message) {
        return getHeaderValues(message, "Content-Encoding");
    }

    public static String getVaryElement(HttpMessage message) {
        return getHeaderValues(message, "Vary");
    }

    public static String getServerElement(HttpMessage message) {
        return getHeaderValues(message, "Server");
    }

    public static String getRemoteAddr(HttpMessage message) {
        return getParam(message, "http.remote.addr");
    }

    /**
     * AccessLogElement writes the partial message into the buffer.
     */
    protected interface AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response);
    }

    /**
     * write local IP address - %A
     */
    protected static class LocalAddrElement implements AccessLogElement {

        private static final String LOCAL_ADDR_VALUE;

        static {
            String init;
            try {
                init = InetAddress.getLocalHost().getHostAddress();
            } catch (Throwable e) {
                AccessTimeUtil.handleThrowable(e);
                init = "127.0.0.1";
            }
            LOCAL_ADDR_VALUE = init;
        }

        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            buf.append(LOCAL_ADDR_VALUE);
        }
    }


    /**
     * write remote host name - %h
     */
    protected static class HostElement implements AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            String remoteHost = "-";
            try {
                if (request != null) {
                    remoteHost = getRemoteAddr(request);
                } else if (response != null) {
                    remoteHost = getRemoteAddr(response);
                }
            } catch (Exception e) {
                // empty host
            }
            buf.append(remoteHost);
        }

    }

    /**
     * write remote logical username from identd (always returns '-') - %l
     */
    protected static class LogicalUserNameElement implements AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            String logicalUserName = "";
            try {
                logicalUserName = getLogicalUserNameElement(request);
            } catch (Exception e) {
                // empty logicalUserName
            }
            buf.append(logicalUserName);
        }
    }


    /**
     * write remote user that was authenticated (if any), else '-' - %u
     */
    protected static class UserElement implements AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            String userElement = "";
            try {
                userElement = getUserNameElement(request);
            } catch (Exception e) {
                // empty UserName
            }
            buf.append(userElement);
        }
    }


    /**
     * write date and time, in Common Log Format - %t
     */
    protected class DateAndTimeElement implements AccessLogElement {

        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {

            String currentDate = AccessTimeUtil.getAccessDate(AccessTimeUtil.getDate());
            buf.append(currentDate);
        }
    }

    /**
     * write first line of the request (method and request URI) - %r
     */
    protected static class RequestElement implements AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            if (request != null) {
                String requestLine = request.getRequestLine().toString();
                buf.append(requestLine);
            } else {
                buf.append("- - ");
            }
        }
    }

    /**
     * write HTTP status code of the response - %s
     */
    protected static class HttpStatusCodeElement implements AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            if (response != null) {
                int statusCode = response.getStatusLine().getStatusCode();
                buf.append(statusCode); //getStatus
            } else {
                buf.append('-');
            }
        }
    }


    /**
     * write bytes sent, excluding HTTP headers - %b, %B
     */
    protected static class ByteSentElement implements AccessLogElement {
        private boolean conversion;

        /**
         * if conversion is true, write '-' instead of 0 - %b
         *
         * @param conversion - To be conversed.
         */
        public ByteSentElement(boolean conversion) {
            this.conversion = conversion;
        }

        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            // Don't need to flush since trigger for log message is after the
            // response has been committed
            long length = -1;
            try {
                if (request != null) {
                    String sLength = getHeaderValues(request, "Content-Length");
                    if(null != sLength && !"".equals(sLength)) {
                        length = Long.valueOf(sLength);
                    }
                } else if (response != null) {
                    length = response.getEntity().getContentLength(); //getBytesWritten(false);
                }

                if (length <= 0 && conversion) {
                    buf.append('-');                           //%b
                } else {
                    buf.append(length);
                }
            } catch (Exception e) {
                buf.append('-'); //No entity found.
            }
        }
    }

    /**
     * write request method (GET, POST, etc.) - %m
     */
    protected static class MethodElement implements AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            if (request != null) {
                buf.append(request.getRequestLine().getMethod());
            }
        }
    }

    /**
     * write requested URL path - %U
     */
    protected static class RequestURIElement implements AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            if (request != null) {
                buf.append(request.getRequestLine().getUri());
            } else {
                buf.append('-');
            }
        }
    }

    /**
     * write local server name - %v
     */
    protected static class LocalServerNameElement implements AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            buf.append(getHeaderValues(request, "server"));
        }
    }

    /**
     * write any string
     */
    protected static class StringElement implements AccessLogElement {
        private String str;

        public StringElement(String str) {
            this.str = str;
        }

        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            buf.append(str);
        }
    }

    /**
     * write incoming headers - %{xxx}i
     */
    protected static class HeaderElement implements AccessLogElement {
        private String header;

        public HeaderElement(String header) {
            this.header = header;
        }

        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            try {
                String value = getHeaderValues(request, header);
                if (value == null) {
                    buf.append('-');
                } else {
                    buf.append(value);
                }
            } catch (Exception e) {
                buf.append('-'); //Header is null
            }
        }
    }

    /**
     * write a specific cookie - %{xxx}c
     */
    protected static class CookieElement implements AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            buf.append(getCookieElement(request));
        }
    }


    /**
     * write the referer - %f
     */
    protected static class RefererElement implements AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            buf.append(getRefererElement(request));
        }
    }


    /**
     * write the user agent - %a
     */
    protected static class UserAgentElement implements AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            buf.append(getUserAgentElement(request));
        }
    }

    /**
     * write the Accept Element - %C
     */
    protected static class AcceptElement implements AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            buf.append(getAcceptElement(request));
        }
    }

    /**
     * write the Accept Language Element - %L
     */
    protected static class AcceptLanguageElement implements AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            buf.append(getAcceptLanguageElement(request));
        }
    }

    /**
     * write the Accept Encoding Element - %e
     */
    protected static class AcceptEncodingElement implements AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            buf.append(getAcceptEncodingElement(request));
        }
    }

    /**
     * write the Accept Character Set Element - %S
     */
    protected static class AcceptCharSetElement implements AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            buf.append(getAcceptCharSetElement(request));
        }
    }

    /**
     * write the Connection Element - %x
     */
    protected static class ConnectionElement implements AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            buf.append(getConnectionElement(request));
        }
    }

    /**
     * write the Content Type Element - %T
     */
    protected static class ContentTypeElement implements AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            buf.append(getContentTypeElement(request));
        }
    }

    /**
     * write the Keep Alive Element - %k
     */
    protected static class KeepAliveElement implements AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            buf.append(getKeepAliveElement(request));
        }
    }

    /**
     * write the Transfer Encoding Element - %E
     */
    protected static class TransferEncodingElement implements AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            buf.append(getTransferEncodingElement(request));
        }
    }

    /**
     * write the Content Encoding Element - %n
     */
    protected static class ContentEncodingElement implements AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            buf.append(getContentEncodingElement(request));
        }
    }

    /**
     * write the Vary Element - %V
     */
    protected static class VaryElement implements AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            buf.append(getVaryElement(request));
        }
    }

    /**
     * write the Server Element - %Z
     */
    protected static class ServerElement implements AccessLogElement {
        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            buf.append(getServerElement(request));
        }
    }

    /**
     * write a specific response header - %{xxx}o
     */
    protected static class ResponseHeaderElement implements AccessLogElement {
        private String header;

        public ResponseHeaderElement(String header) {
            this.header = header;
        }

        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            if (null != response) {
                buf.append(getHeaderValues(response, header));
            }
            buf.append("-");
        }
    }

    /**
     * write an attribute in the ServletRequest - %{xxx}r    %R
     */
    protected static class RequestAttributeElement implements AccessLogElement {

        public RequestAttributeElement() {
        }

        public void addElement(StringBuilder buf, Date date, HttpRequest request,
                               HttpResponse response) {
            Object value;
            if (request != null) {
                value = request.getLastHeader(buf.toString()); //gets the attribute header
            } else {
                value = "??";
            }
            if (value != null) {
                if (value instanceof String) {
                    buf.append((String) value);
                } else {
                    buf.append(value.toString());
                }
            } else {
                buf.append('-');
            }
        }

    }


    /**
     * parse pattern string and create the array of AccessLogElement
     *
     * @return Array of AccessLogElement
     */
    protected AccessLogElement[] createLogElements() {
        List<AccessLogElement> list = new ArrayList<AccessLogElement>();
        boolean replace = false;
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < pattern.length(); i++) {
            char ch = pattern.charAt(i);
            if (replace) {
                /*
                 * For code that processes {, the behavior will be ... if I do
                 * not encounter a closing } - then I ignore the {
                 */
                if ('{' == ch) {
                    StringBuilder name = new StringBuilder();
                    int j = i + 1;
                    for (; j < pattern.length() && '}' != pattern.charAt(j); j++) {
                        name.append(pattern.charAt(j));
                    }
                    if (j + 1 < pattern.length()) {
                        /* the +1 was to account for } which we increment now */
                        j++;
                        list.add(createAccessLogElement(name.toString(),
                                                        pattern.charAt(j)));
                        i = j; /* Since we walked more than one character */
                    } else {
                        // D'oh - end of string - pretend we never did this
                        // and do processing the "old way"
                        list.add(createAccessLogElement(ch));
                    }
                } else {
                    list.add(createAccessLogElement(ch));
                }
                replace = false;
            } else if (ch == '%') {
                replace = true;
                list.add(new StringElement(buf.toString()));
                buf = new StringBuilder();
            } else {
                buf.append(ch);
            }
        }
        if (buf.length() > 0) {
            list.add(new StringElement(buf.toString()));
        }
        return list.toArray(new AccessLogElement[list.size()]);
    }

    /**
     * create an AccessLogElement implementation which needs header string
     *
     * @param header  - header of the request/response
     * @param pattern - pattern character given for the input element
     * @return AccessLogElement - accessLogElement
     */
    private AccessLogElement createAccessLogElement(String header, char pattern) {
        switch (pattern) {
            case 'i':
                return new HeaderElement(header);  //%{xxx}i
            case 'o':
                return new ResponseHeaderElement(header);
            case 'R':
                return new RequestAttributeElement();
            default:
                return new StringElement("???");
        }
    }

    /**
     * create an AccessLogElement implementation
     *
     * @param pattern - pattern character given for the input element
     * @return AccessLogElement acceessLogElement
     */
    private AccessLogElement createAccessLogElement(char pattern) {
        switch (pattern) {
            case 'A':
                return new LocalAddrElement();
            case 'a':
                return new UserAgentElement();
            case 'b':
                return new ByteSentElement(true);     //%b
            case 'B':
                return new ByteSentElement(false);
            case 'c':
                return new CookieElement();            // %c
            case 'C':
                return new AcceptElement();
            case 'e':
                return new AcceptEncodingElement();
            case 'E':
                return new TransferEncodingElement();
            case 'f':
                return new RefererElement();
            case 'h':
                return new HostElement();         //%h
            case 'k':
                return new KeepAliveElement();
            case 'l':
                return new LogicalUserNameElement();     //%l
            case 'L':
                return new AcceptLanguageElement();
            case 'm':
                return new MethodElement();
            case 'n':
                return new ContentEncodingElement();
            case 'r':
                return new RequestElement();        //%r
            case 'S':
                return new AcceptCharSetElement();
            case 's':
                return new HttpStatusCodeElement();       // %s
            case 'T':
                return new ContentTypeElement();
            case 't':
                return new DateAndTimeElement();       //%t
            case 'u':
                return new UserElement();           //%u
            case 'U':
                return new RequestURIElement();
            case 'V':
                return new VaryElement();
            case 'v':
                return new LocalServerNameElement();
            case 'x':
                return new ConnectionElement();
            case 'Z':
                return new ServerElement();
            default:
                return new StringElement("???" + pattern + "???");
        }
    }
}
