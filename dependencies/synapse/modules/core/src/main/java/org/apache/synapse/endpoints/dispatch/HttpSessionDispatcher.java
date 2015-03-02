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

package org.apache.synapse.endpoints.dispatch;

import org.apache.http.protocol.HTTP;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * Dispatches sessions based on HTTP cookies. Session is initiated by the server in the first
 * response when it sends "Set-Cookie" HTTP header with the session ID. For all successive messages
 * client should send "Cookie" HTTP header with session ID send by the server.
 */
public class HttpSessionDispatcher extends AbstractDispatcher {


    /*HTTP Headers  */
    private final static String COOKIE = "Cookie";
    private final static String SET_COOKIE = "Set-Cookie";
    public static final String HOSTS = "hosts";

    /**
     * Check if "Cookie" HTTP header is available. If so, check if that cookie is in the session
     * map. If cookie is available, there is a session for this cookie. return the (server)
     * endpoint for that session.
     *
     * @param synCtx MessageContext possibly containing a "Cookie" HTTP header.
     * @return Endpoint Server endpoint for the given HTTP session.
     */
    public SessionInformation getSession(MessageContext synCtx) {
        String hostName = extractHost(synCtx);
        if (log.isDebugEnabled()) {
            log.debug("Extracted Host Name : " + hostName);
        }

        // print TO
        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();
        if (log.isDebugEnabled()) {
            log.debug("Endpoint Address : " + axis2MessageContext.getTo().getAddress());
        }

        Map headerMap = getTransportHeaderMap(synCtx);
        String contentType = (String)headerMap.get("Content-Type");
        if (log.isDebugEnabled()) {
            log.debug("Content Type : " + contentType);
        }

        if (hostName == null) {
            return SALSessions.getInstance().getSession(extractSessionID(synCtx, COOKIE));
        } else {
            List<String> sessionList = extractSessionIDs(synCtx, COOKIE);
            if (sessionList != null) {
                for (String sessionID : sessionList) {
                    SessionInformation sessionInfoObj = SALSessions.getInstance().getSession(sessionID);
                     if (sessionInfoObj != null && sessionInfoObj.getMember() != null) {
                        Map<String, String> subDomainNames =
                                (Map<String, String>) sessionInfoObj.getMember().getProperties().get(HOSTS);
                        if (log.isDebugEnabled()) {
                            log.debug("Member Domain : " + (subDomainNames != null ? subDomainNames.get(hostName) : null) +
                                      " : Session ID " + sessionID);
                        }
                        if (subDomainNames != null && subDomainNames.get(hostName) != null) {
                            if (log.isDebugEnabled()) {
                                log.debug("Found a matching sessionInfo Object for the " + hostName);
                            }
                            return sessionInfoObj;
                        }
					} else if (sessionInfoObj != null
							&& sessionInfoObj.getMember() == null) {
						// looks like a session attached to a failed member.Just return the session in this case
						if (log.isDebugEnabled()) {
							log.debug("sessionInfo object["	+ sessionInfoObj.getId()+ "] found with a null member. "
									+ "Looks like this is attached to a failed member.");
						}
						return sessionInfoObj;
                     }
                }
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("Did not find a session info obj.");
        }
        return null;
    }

    /**
     * Searches for "Set-Cookie" HTTP header in the message context. If found and that given
     * session ID is not already in the session map update the session map by mapping the cookie
     * to the endpoint.
     *
     * @param synCtx MessageContext possibly containing the "Set-Cookie" HTTP header.
     */
	public void updateSession(MessageContext synCtx) {

		SessionCookie cookie = extractSessionCookie(synCtx, SET_COOKIE);

		if (cookie != null) {
			if (log.isDebugEnabled()) {
				log.debug("Found the HTTP header [Set-Cookie]: " + cookie.toString() + "' for updating the session");
			}

			SALSessions.getInstance().updateSession(synCtx, cookie);
		}

	}

    public void unbind(MessageContext synCtx) {
        SALSessions.getInstance().removeSession(extractSessionID(synCtx, COOKIE));
    }

    /**
     * HTTP sessions are initiated by the server.
     *
     * @return true
     */
    public boolean isServerInitiatedSession() {
        return true;
    }

    public void removeSessionID(MessageContext syCtx) {
        removeSessionID(syCtx, COOKIE);
    }

    protected List<String> extractSessionIDs(MessageContext synCtx, String key) {
        List<String> sessionList = new ArrayList<String>();
        if (key != null) {
            Map headerMap = getTransportHeaderMap(synCtx);
            if (headerMap != null) {
                Object hostObj = headerMap.get("Host");
                if (log.isDebugEnabled()) {
                    log.debug("A request received with the Host Name : " + (String) hostObj);
                }
                Object cookieObj = headerMap.get(key);
                if (cookieObj instanceof String) {
                    String cookie = (String) cookieObj;
                    if (log.isDebugEnabled()) {
                        log.debug("Cookies String : " + cookie);
                    }
                    // extract the first name value pair of the Set-Cookie header, which is considered
                    // as the session id which will be sent back from the client with the Cookie header
                    // for example;
                    //      Set-Cookie: JSESSIONID=760764CB72E96A7221506823748CF2AE; Path=/
                    // will result in the session id "JSESSIONID=760764CB72E96A7221506823748CF2AE"
                    String[] sessionIds = cookie.split(";");
                    if (sessionIds == null || sessionIds.length == 0) {
                        if (log.isDebugEnabled()) {
                            log.debug("Cannot find a session id for the cookie : " + cookie);
                        }
                        return null;
                    }
                    for(String sessionId : sessionIds){
						if (sessionId != null
								&& (sessionId.indexOf("JSESSIONID") != -1 || sessionId
										.indexOf("PHPSESSID") != -1 || sessionId.indexOf("phpMyAdmin") != -1 || 
                                sessionId.indexOf("wordpress_test_cookie") != -1)) {
							if (log.isDebugEnabled()) {
								log.debug("Extracted SessionID : " + sessionId);
							}
							sessionList.add(sessionId.trim());
						}
                    }
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Couldn't find the " + key + " header to find the session");
                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Couldn't find the TRANSPORT_HEADERS to find the session");
                }

            }
        }
        return sessionList;
    }

    private String extractHost(MessageContext synCtx) {
        Map headerMap = getTransportHeaderMap(synCtx);
        String hostName = null;
        if (headerMap != null) {
            Object hostObj = headerMap.get(HTTP.TARGET_HOST);
            hostName = (String) hostObj;
            if (hostName != null && hostName.contains(":")) {
                hostName = hostName.substring(0, hostName.indexOf(":"));
            }
        }
        return hostName;
    }

    private Map getTransportHeaderMap(MessageContext synCtx) {

        org.apache.axis2.context.MessageContext axis2MessageContext =
                ((Axis2MessageContext) synCtx).getAxis2MessageContext();

        Object o = axis2MessageContext.getProperty(org.apache.axis2.context.MessageContext.TRANSPORT_HEADERS);
        if (o != null && o instanceof Map) {
            return (Map) o;
        }
        return null;
    }
}
