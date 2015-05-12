/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.throttle.module;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.transport.http.HTTPConstants;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.Principal;
import java.util.*;

public class TestMessageContext extends MessageContext{

    private static String[] TEST_REMOTE_HOSTS = {"www.abc.com","www.wso2.org","www.test.com/lk"};
    private static String[] TEST_IPS = {"10.100.1.162","10.100.2.161","10.100.5.105"};
    private static String[] TEST_OAUTH_HEADERS = {"oauth_consumer_key=\"nq21LN39VlKe6OezcOndBx\", oauth_signature_method=\"HMAC-SHA1\", oauth_signature=\"DZKyT75hiOIdtMGCU%2BbITArs4sU%3D\", oauth_timestamp=\"1328590467\", oauth_nonce=\"7031216264696\", oauth_version=\"1.0\"",
                                                  "oauth_consumer_key=\"kkkkksdsds22222444BASS\", oauth_signature_method=\"HMAC-SHA1\", oauth_signature=\"DZKyT75hiOIdtMGCU%2BbITArs4sU%3D\", oauth_timestamp=\"1328590467\", oauth_nonce=\"7031216264696\", oauth_version=\"1.0\"",
                                                  "oauth_consumer_key=\"assds222323sasdadadFFF\", oauth_signature_method=\"HMAC-SHA1\", oauth_signature=\"DZKyT75hiOIdtMGCU%2BbITArs4sU%3D\", oauth_timestamp=\"1328590467\", oauth_nonce=\"7031216264696\", oauth_version=\"1.0\"",
                                                  "oauth_consumer_key=\"11111111ds22222444BASS\", oauth_signature_method=\"HMAC-SHA1\", oauth_signature=\"DZKyT75hiOIdtMGCU%2BbITArs4sU%3D\", oauth_timestamp=\"1328590467\", oauth_nonce=\"7031216264696\", oauth_version=\"1.0\"",
                                                 };

    public static void setTestIPs(String[] ips){
        TEST_IPS = ips;
    }

    public static void setTestRemoteHosts(String[] hosts){
        TEST_REMOTE_HOSTS = hosts;
    }

    public Object getPropertyNonReplicable(String key) {
        HttpServletRequest request = null;
        if(key.equals(HTTPConstants.MC_HTTP_SERVLETREQUEST)){
            request = new HttpServletRequest() {
                public String getAuthType() {
                    return null;
                }

                public Cookie[] getCookies() {
                    return new Cookie[0];
                }

                public long getDateHeader(String s) {
                    return 0;
                }

                public String getHeader(String s) {
                      Random rnd = new Random();
                      return TEST_OAUTH_HEADERS[rnd.nextInt(TEST_OAUTH_HEADERS.length)];
//                    return "oauth_consumer_key=\"nq21LN39VlKe6OezcOndBx\", oauth_signature_method=\"HMAC-SHA1\", oauth_signature=\"DZKyT75hiOIdtMGCU%2BbITArs4sU%3D\", oauth_timestamp=\"1328590467\", oauth_nonce=\"7031216264696\", oauth_version=\"1.0\"";
                }

                public Enumeration getHeaders(String s) {
                    return null;
                }

                public Enumeration getHeaderNames() {
                    return null;
                }

                public int getIntHeader(String s) {
                    return 0;
                }

                public String getMethod() {
                    return null;
                }

                public String getPathInfo() {
                    return null;
                }

                public String getPathTranslated() {
                    return null;
                }

                public String getContextPath() {
                    return null;
                }

                public String getQueryString() {
                    return null;
                }

                public String getRemoteUser() {
                    return null;
                }

                public boolean isUserInRole(String s) {
                    return false;
                }

                public Principal getUserPrincipal() {
                    return null;
                }

                public String getRequestedSessionId() {
                    return null;
                }

                public String getRequestURI() {
                    return null;
                }

                public StringBuffer getRequestURL() {
                    return null;
                }

                public String getServletPath() {
                    return null;
                }

                public HttpSession getSession(boolean b) {
                    return null;
                }

                public HttpSession getSession() {
                    return null;
                }

                public boolean isRequestedSessionIdValid() {
                    return false;
                }

                public boolean isRequestedSessionIdFromCookie() {
                    return false;
                }

                public boolean isRequestedSessionIdFromURL() {
                    return false;
                }

                public boolean isRequestedSessionIdFromUrl() {
                    return false;
                }

                public boolean authenticate(HttpServletResponse httpServletResponse) throws IOException, ServletException {
                    return false;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public void login(String s, String s1) throws ServletException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                public void logout() throws ServletException {
                    //To change body of implemented methods use File | Settings | File Templates.
                }

                public Collection<Part> getParts() throws IOException, ServletException {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public Part getPart(String s) throws IOException, ServletException {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public Object getAttribute(String s) {
                    return null;
                }

                public Enumeration getAttributeNames() {
                    return null;
                }

                public String getCharacterEncoding() {
                    return null;
                }

                public void setCharacterEncoding(String s) throws UnsupportedEncodingException {

                }

                public int getContentLength() {
                    return 0;
                }

                public String getContentType() {
                    return null;
                }

                public ServletInputStream getInputStream() throws IOException {
                    return null;
                }

                public String getParameter(String s) {
                    return null;
                }

                public Enumeration getParameterNames() {
                    return null;
                }

                public String[] getParameterValues(String s) {
                    return new String[0];
                }

                public Map getParameterMap() {
                    return null;
                }

                public String getProtocol() {
                    return null;
                }

                public String getScheme() {
                    return null;
                }

                public String getServerName() {
                    return null;
                }

                public int getServerPort() {
                    return 0;
                }

                public BufferedReader getReader() throws IOException {
                    return null;
                }

                public String getRemoteAddr() {
                    return null;
                }

                public String getRemoteHost() {
                    Random rnd = new Random();
                    return TEST_REMOTE_HOSTS[rnd.nextInt(TEST_REMOTE_HOSTS.length)];
                }

                public void setAttribute(String s, Object o) {

                }

                public void removeAttribute(String s) {

                }

                public Locale getLocale() {
                    return null;
                }

                public Enumeration getLocales() {
                    return null;
                }

                public boolean isSecure() {
                    return false;
                }

                public RequestDispatcher getRequestDispatcher(String s) {
                    return null;
                }

                public String getRealPath(String s) {
                    return null;
                }

                public int getRemotePort() {
                    return 0;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public String getLocalName() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public String getLocalAddr() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public int getLocalPort() {
                    return 0;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public ServletContext getServletContext() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public AsyncContext startAsync() throws IllegalStateException {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public boolean isAsyncStarted() {
                    return false;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public boolean isAsyncSupported() {
                    return false;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public AsyncContext getAsyncContext() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }

                public DispatcherType getDispatcherType() {
                    return null;  //To change body of implemented methods use File | Settings | File Templates.
                }
            };

        }

        return request;
    }

    public Object getProperty(String key) {
        if(MessageContext.REMOTE_ADDR.equals(key)){
            Random rnd = new Random();
            return TEST_IPS[rnd.nextInt(TEST_IPS.length)];
        }
        return null;
    }

    public static void main(String[] args) {
        TestMessageContext.setTestIPs(new String[]{"10.192.1.10", "10.199.1.1", "10.100.5.1",
                                                   "10.101.15.1"});
        for (String s : TEST_OAUTH_HEADERS) {
            System.out.println(s);
        }
        TestMessageContext msgCtxt = new TestMessageContext();
        HttpServletRequest request =
                (HttpServletRequest) msgCtxt.getPropertyNonReplicable(
                        HTTPConstants.MC_HTTP_SERVLETREQUEST);


        System.out.println(request.getRemoteHost());
        System.out.println(request.getRemoteHost());
        System.out.println(request.getRemoteHost());
        System.out.println(request.getRemoteHost());
        System.out.println(request.getRemoteHost());
        System.out.println(request.getRemoteHost());
        System.out.println(request.getRemoteHost());
        System.out.println(request.getRemoteHost());
        System.out.println(request.getRemoteHost());

        System.out.println(msgCtxt.getProperty(MessageContext.REMOTE_ADDR));
        System.out.println(msgCtxt.getProperty(MessageContext.REMOTE_ADDR));
        System.out.println(msgCtxt.getProperty(MessageContext.REMOTE_ADDR));
        System.out.println(msgCtxt.getProperty(MessageContext.REMOTE_ADDR));
        System.out.println(msgCtxt.getProperty(MessageContext.REMOTE_ADDR));
        System.out.println(msgCtxt.getProperty(MessageContext.REMOTE_ADDR));
        System.out.println(msgCtxt.getProperty(MessageContext.REMOTE_ADDR));
        System.out.println(msgCtxt.getProperty(MessageContext.REMOTE_ADDR));
        System.out.println(msgCtxt.getProperty(MessageContext.REMOTE_ADDR));
        System.out.println(msgCtxt.getProperty(MessageContext.REMOTE_ADDR));
    }

}
