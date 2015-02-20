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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.clustering.Member;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.endpoints.Endpoint;
import org.apache.synapse.endpoints.IndirectEndpoint;
import org.apache.synapse.endpoints.SALoadbalanceEndpoint;
import org.apache.synapse.endpoints.DynamicLoadbalanceEndpoint;
import org.apache.synapse.util.Replicator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Keeps the states of the sessions
 */
public class SALSessions {

    private static final SALSessions INSTANCE = new SALSessions();

    private static final Log log = LogFactory.getLog(SALSessions.class);

    private final static String SESSION_IDS = "synapse.salep.sessionids.";

    private ConfigurationContext configCtx;

    /* Is this env. support clustering*/
    private boolean isClustered = false;

    private boolean initialized = false;

    /*Cache all path with its endpoint sequence. This is only need for a clustered environment */
    private final Map<List<String>, List<Endpoint>> namesToEndpointsMap =
            new HashMap<List<String>, List<Endpoint>>();

    /* Non- clustered environment , all the established sessions*/
    private final Map<String, SessionInformation> establishedSessions =
            new ConcurrentHashMap<String, SessionInformation>();
    /* all child endpoints .  This is only need for a clustered environment*/
    private final Map<String, Map<String, Endpoint>> childEndpoints =
            new HashMap<String, Map<String, Endpoint>>();

    private SALSessions() {
    }

    public static SALSessions getInstance() {
        return INSTANCE;
    }

    /**
     * Initialize SALSessions instance
     *
     * @param isClusteringEnable is this a clustered environment
     * @param cc                 Axis config context
     */
    public void initialize(boolean isClusteringEnable, ConfigurationContext cc) {

        if (!initialized) {
            if (log.isDebugEnabled()) {
                log.debug("Initializing SALSessions instance. Environment : " +
                        (isClusteringEnable ? " clustered" : " local"));
            }
            if (isClusteringEnable) {
                isClustered = isClusteringEnable;
                configCtx = cc;
            }
            initialized = true;
        }
    }

    /**
     * This method only use in a clustered environment.
     *
     * @param endpoint  Root endpoint name
     * @param endpoints children
     */
    public void registerChildren(Endpoint endpoint, List<Endpoint> endpoints) {

        if (isClustered) {

            String endpointName = endpoint.getName();

            validateInput(endpointName);

            if (log.isDebugEnabled()) {
                log.debug("Registering endpoints " + endpoints + " of " + endpointName);
            }

            if (!childEndpoints.containsKey(endpointName)) {

                Map<String, Endpoint> children = new HashMap<String, Endpoint>();
                children.put(endpointName, endpoint);
                fillMap(endpoints, children);
                childEndpoints.put(endpointName, children);

            }
        }

    }

    /**
     * Update or establish a session
     *
     * @param synCtx    Synapse MessageContext
     * @param sessionID session id
     */
    public void updateSession(MessageContext synCtx, String sessionID) {

        if (sessionID == null || "".equals(sessionID)) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot find session ID .Returing null");
            }
            return;
        }

        boolean createSession = false;

        //if this is related to the already established session
        SessionInformation oldSession = (SessionInformation) synCtx.getProperty(
                SynapseConstants.PROP_SAL_CURRENT_SESSION_INFORMATION);

        List<Endpoint> endpoints = null;
        Member currentMember = null; 

        if (oldSession == null) {

            if (log.isDebugEnabled()) {
                log.debug("Going to create a New session with id  " + sessionID);
            }
            endpoints = (List<Endpoint>) synCtx.getProperty(
                    SynapseConstants.PROP_SAL_ENDPOINT_ENDPOINT_LIST);

            currentMember = (Member) synCtx.getProperty(
                    SynapseConstants.PROP_SAL_ENDPOINT_CURRENT_MEMBER);
            
            createSession = true;
        } else {

            String oldSessionID = oldSession.getId();
            if (!sessionID.equals(oldSessionID)) {

                if (log.isDebugEnabled()) {
                    log.debug("Renew the session : previous session id :" +
                            oldSessionID + " new session id :" + sessionID);
                }
                removeSession(oldSessionID);
                endpoints = oldSession.getEndpointList();
                currentMember = oldSession.getMember();
                createSession = true;

            } else {

                SessionInformation information = getSessionInformation(oldSessionID);
                if (information == null) {
                    // This means , our session information has been removed during getting response.
                    // Therefore, it is recovered using session information in the message context
                    if (log.isDebugEnabled()) {
                        log.debug("Recovering lost session information for session id " + sessionID);
                    }
                    endpoints = oldSession.getEndpointList();
                    currentMember = oldSession.getMember();
                    createSession = true;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("Session with id : " + sessionID + " is still live.");
                    }
                }
            }
        }

        if (createSession) {
            SessionInformation newInformation;
            if(currentMember == null){
                newInformation = createSessionInformation(synCtx, sessionID, endpoints);
            } else {
                newInformation = createSessionInformation(synCtx, sessionID, currentMember, null);
            }

            if (log.isDebugEnabled()) {
                log.debug("Establishing a session with id :" +
                        sessionID + " and it's endpoint sequence : " + endpoints);
            }

            if (isClustered) {
                Replicator.setAndReplicateState(SESSION_IDS + sessionID, newInformation, configCtx);
            } else {
                establishedSessions.put(sessionID, newInformation);
            }
        }
    }
    
	/**
	 * Update or establish a session
	 * 
	 * @param synCtx
	 *            Synapse MessageContext
	 * @param sessionID
	 *            session id
	 */
	public void updateSession(MessageContext synCtx, SessionCookie cookie) {

		if (cookie == null || "".equals(cookie.getSessionId())) {
			if (log.isDebugEnabled()) {
				log.debug("Cannot find Session ID from the cookie.");
			}
			return;
		}

		String sessionId = cookie.getSessionId();
		String path = cookie.getPath();

		boolean createSession = false;

		if (log.isDebugEnabled()) {
			log.debug("Starting to update the session for : " + cookie);
		}

		// if this is related to the already established session
		SessionInformation oldSession = (SessionInformation) synCtx
				.getProperty(SynapseConstants.PROP_SAL_CURRENT_SESSION_INFORMATION);

		List<Endpoint> endpoints = null;
		Member currentMember = null;

		if (oldSession == null) {
			if (log.isDebugEnabled()) {
				log.debug("Going to create a New session with corresponds to: " + cookie);
			}
			endpoints = (List<Endpoint>) synCtx.getProperty(SynapseConstants.PROP_SAL_ENDPOINT_ENDPOINT_LIST);

			currentMember = (Member) synCtx.getProperty(SynapseConstants.PROP_SAL_ENDPOINT_CURRENT_MEMBER);

			createSession = true;
		} else {
			String oldSessionID = oldSession.getId();
			// This assumes that there can only be one path
			if (!sessionId.equals(oldSessionID) && pathMatches(path, oldSession.getPath())) {

				if (log.isDebugEnabled()) {
					log.debug("Renew the session : previous session id :" + oldSessionID + " new session :" + cookie);
				}
				removeSession(oldSessionID);
				endpoints = oldSession.getEndpointList();
				currentMember = oldSession.getMember();
				createSession = true;

			} else {

				SessionInformation information = getSessionInformation(oldSessionID);
				if (information == null) {
					// This means , our session information has been removed
					// during getting response.
					// Therefore, it is recovered using session information in
					// the message context
					if (log.isDebugEnabled()) {
						log.debug("Recovering lost session information for session id " + sessionId);
					}
					endpoints = oldSession.getEndpointList();
					currentMember = oldSession.getMember();
					createSession = true;
				} else {
					if (log.isDebugEnabled()) {
						log.debug("Session with id : " + sessionId + " is still live.");
					}
				}
			}
		}

		if (createSession) {
			SessionInformation newInformation;

			List<String> paths = new ArrayList<String>();
			// add the new path
			paths.add(path);

			if (currentMember == null) {
				newInformation = createSessionInformation(synCtx, sessionId, endpoints, paths);
			} else {
				newInformation = createSessionInformation(synCtx, sessionId, currentMember, paths);
			}

			if (log.isDebugEnabled()) {
				log.debug("Establishing a session for :" + cookie + " and it's endpoint sequence : " + endpoints);
			}

			if (isClustered) {
				Replicator.setAndReplicateState(SESSION_IDS + sessionId, newInformation, configCtx);
			} else {
				establishedSessions.put(sessionId, newInformation);
			}
		}
	}

	private boolean pathMatches(String path, List<String> pathList) {
		for (String aPath : pathList) {
			if (aPath != null && aPath.equals(path)) {
				return true;
			}
		}
		return false;
	}

    /**
     * return the endpoint  for the given session.
     * Null will be returned , if there is no endpoint for given session.
     *
     * @param sessionID The session identifier
     * @return Returns the endpoint for the given session.
     */
    public SessionInformation getSession(String sessionID) {

        if (sessionID == null || "".equals(sessionID)) {
            if (log.isDebugEnabled()) {
                log.debug("Cannot find session ID .Returning null");
            }
            return null;
        }

        if (log.isDebugEnabled()) {
            log.debug("Retrieving the endpoint from the session id " + sessionID);
        }

        SessionInformation information = getSessionInformation(sessionID);
        if (information == null) {
            if (log.isDebugEnabled()) {
                log.debug("Session information cannot be found for session id " + sessionID);
            }
            return null;
        }

        if (information.isExpired()) {
            if (log.isDebugEnabled()) {
                log.debug("Session has been expired for session with id: " + sessionID);
            }
            removeSession(sessionID);
            return null;
        }

        return information;
    }

    /**
     * Returns endpoint sequence related to the given session
     *
     * @param information Session information
     * @return endpoint sequence
     */
    public List<Endpoint> getChildEndpoints(SessionInformation information) {

        List<Endpoint> endpoints;
        if (isClustered) {
            endpoints =
                    getEndpoints(information.getPath(), information.getRootEndpointName());
        } else {
            endpoints = information.getEndpointList();
        }
        if (log.isDebugEnabled()) {
            log.debug("Retrieving endpoint sequence : " + endpoints +
                    " for session " + information.getId());
        }

        if (endpoints == null || endpoints.isEmpty()) {
            handleException("Session with id " + information.getId() + " is invalid ." +
                    " A session must have a endpoint sequence associated with it");
        }

        List<Endpoint> toBeSent = new ArrayList<Endpoint>();
        toBeSent.addAll(endpoints);
        //remove the root as only expect children
        toBeSent.remove(0);

        return toBeSent;
    }

    /**
     * Removes the endpoint for the given session.
     *
     * @param sessionId The session identifier
     */
    public void removeSession(String sessionId) {

        if (sessionId == null || "".equals(sessionId)) {
            if (log.isDebugEnabled()) {
                log.debug("Session Id cannot be found.The session will not be removed.");
            }
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("Removing the session with the session Id " + sessionId);
        }

        if (isClustered) {
            Replicator.removeAndReplicateState(SESSION_IDS + sessionId, configCtx);

        } else {
            establishedSessions.remove(sessionId);
        }
    }

    /**
     * Clear all the expired sessions
     */
    public synchronized void clearSessions() {
    	
    	List<String> toBeRemoved = null;

        if (!initialized) {
            return;
        }

        try {
            if (isClustered) {

                toBeRemoved = new ArrayList<String>();
                for (Iterator<String> props = configCtx.getPropertyNames(); props.hasNext();) {
                    Object name = props.next();

                    if (name instanceof String && ((String) name).startsWith(SESSION_IDS)) {
                        String key = (String) name;
                        SessionInformation info = (SessionInformation) configCtx.getProperty(key);

                        if (info != null && info.isExpired()) {
                            if (log.isDebugEnabled()) {
                                log.debug("Clustered Environment :" +
                                        "Expired session with id :" + key);
                            }

                            toBeRemoved.add(key);
                        }
                    }
                }

                if (!toBeRemoved.isEmpty()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Clearing expired sessions");
                    }

                    for (String key : toBeRemoved) {
                        Replicator.removeAndReplicateState(key, configCtx);
                    }
                }

            } else {

                toBeRemoved = new ArrayList<String>();
                for (SessionInformation information : establishedSessions.values()) {

                    if (information != null && information.isExpired()) {
                        String id = information.getId();
                        if (log.isDebugEnabled()) {
                            log.debug("Expired session with id :" + id);
                        }
                        toBeRemoved.add(id);
                    }
                    // this is a safe precaution to avoid adding to ArrayList going OoM
                    if(toBeRemoved.size() > 10000){
                    	removeSessions(toBeRemoved);
                    }
                }
                removeSessions(toBeRemoved);

            }
        } catch (Throwable ignored) {
            log.debug("Ignored error clearing sessions : Error " + ignored);
        }
        finally{
        	if(toBeRemoved != null){
        		toBeRemoved = null;
        	}
        }
        
    }

    private void removeSessions(List<String> toBeRemoved) {

    	if (!toBeRemoved.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("Clearing expired sessions");
            }
            establishedSessions.keySet().removeAll(toBeRemoved);
        }
    	
    	toBeRemoved.clear();
	}

	public boolean isInitialized() {
        return initialized;
    }

    /**
     * Helper methods for handle errors.
     *
     * @param msg The error message
     */
    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    public void reset() {

        if (!initialized) {
            return;
        }
        
        log.info("Clearing all states ");
        initialized = false;
        establishedSessions.clear();
        namesToEndpointsMap.clear();
        childEndpoints.clear();
    }
    /*
     * Helper method to get a map from a list - This is for clustered env.
     */
    private void fillMap(List<Endpoint> endpoints, Map<String, Endpoint> endpointsMap) {

        if (endpoints != null) {
            for (Endpoint endpoint : endpoints) {

                String endpointName = getEndpointName(endpoint);
                if (endpointsMap.containsKey(endpointName)) {
                    handleException("Endpoint Name with ' " + endpointName + "' already there. " +
                            "Endpoint name must be unique.");
                }
                endpointsMap.put(endpointName, endpoint);
                fillMap(endpoint.getChildren(), endpointsMap);
            }
        }
    }

    /*
    * Helper method to get a name of endpoints from a endpoint list - This is for clustered env.
    */
    private List<String> getEndpointNames(List<Endpoint> endpoints) {

        List<String> endpointNames = new ArrayList<String>();
        for (Endpoint endpoint : endpoints) {
            endpointNames.add(getEndpointName(endpoint));
        }
        return endpointNames;
    }

    /*
     * Helper method to get a list of endpoints from a list of endpoint name maps -
     * This is for clustered env.
     */
    private List<Endpoint> getEndpoints(List<String> endpointNames, String root) {

        if (endpointNames == null || endpointNames.isEmpty()) {
            handleException("Invalid session - path cannot be null.");

        }
        if (log.isDebugEnabled()) {
            log.debug("Retrieving endpoint sequence for path " + endpointNames);
        }
        List<Endpoint> endpoints = new ArrayList<Endpoint>();
        // First looking at cache - we cache path -> endpoint list . o.w It is a cost
        // to each time calculate
        if (namesToEndpointsMap.containsKey(endpointNames)) {
            endpoints.addAll(namesToEndpointsMap.get(endpointNames));
            return endpoints;
        }

        Map<String, Endpoint> map = childEndpoints.get(root);
        assert endpointNames != null;
        for (String endpointName : endpointNames) {
            Endpoint endpoint = null;
            if (map != null) {
                endpoint = map.get(endpointName);
                if (endpoint == null || endpoints.contains(endpoint)) {
                    map = childEndpoints.get(endpointName);
                    if (map != null) {
                        endpoint = map.get(endpointName);
                    }
                }
            }
            if (endpoint == null) {
                handleException("Invalid session. Endpoint with name '" +
                        endpointName + "' cannot found");
            }
            endpoints.add(endpoint);
        }
        //cache path(endpoint names) vs endpoint (instance) sequence
        namesToEndpointsMap.put(endpointNames, endpoints);

        return endpoints;
    }

    /**
     * Updates sessions corresponds to a particular member from the {@link #establishedSessions}
     * @param member subjected {@link Member}
     */
    public void removeSessionsOfMember(Member member){
        for (Iterator<SessionInformation> iterator = establishedSessions.values().iterator(); iterator.hasNext();) {
        	SessionInformation info = iterator.next();

            if (member.equals(info.getMember())) {
            	//We don't want to lose sessions of this member. So we merely set the member to null and
            	//put it back to establishedSessions so that those can be used later.
            	info.setMember(null);
                iterator.remove();
                
                establishedSessions.put(info.getId(),info);
                log.debug("Session associated with member " + member.toString() +
                		 " is updated ; session id : " + info.getId());
            }
        }
    }

	/*
     * Validate endpoint name
     */
    private void validateInput(String endpointName) {

        if (endpointName == null) {
            handleException("For proper clustered mode operation, " +
                    "all endpoints should be uniquely named");
        }
    }

    /*
     * Returns an endpoint name for the endpoint object -  This is for clustered env.
     */
    private String getEndpointName(Endpoint endpoint) {

        if (endpoint == null) {
            handleException("Endpoint cannot be null.");
        }

        assert endpoint != null;
        String endpointName = endpoint.getName();
        if (endpointName == null && endpoint instanceof IndirectEndpoint) {
            endpointName = ((IndirectEndpoint) endpoint).getKey();
        }
        validateInput(endpointName);
        return endpointName;
    }

    /*
     * Returns a session information for given session id
     */
    private SessionInformation getSessionInformation(String sessionID) {

        if (isClustered) {
            return (SessionInformation)
                    configCtx.getPropertyNonReplicable(SESSION_IDS + sessionID);
        } else {
            return establishedSessions.get(sessionID);
        }
    }

    /*
     * Factory method to create a session information using given endpoint list,
     * session id and other informations
     */
    private SessionInformation createSessionInformation(MessageContext synCtx,
                                                        String id, List<Endpoint> endpoints) {

        if (endpoints == null || endpoints.isEmpty()) {
            handleException("Invalid request to create sessions . Cannot find a endpoint sequence.");
        }

        if (log.isDebugEnabled()) {
            log.debug("Creating a session information for given session id  " + id
                    + " with endpoint sequence " + endpoints);
        }

        long expireTimeWindow = -1;
        if(endpoints != null) {
        	for (Endpoint endpoint : endpoints) {

        		if (endpoint instanceof SALoadbalanceEndpoint) {
        			long sessionsTimeout = ((SALoadbalanceEndpoint) endpoint).getSessionTimeout();

        		    if (expireTimeWindow == -1) {
        			    expireTimeWindow = sessionsTimeout;
        		    } else if (expireTimeWindow > sessionsTimeout) {
        			    expireTimeWindow = sessionsTimeout;
        		    }
                }
            }
        }

        if (expireTimeWindow == -1) {
            expireTimeWindow = synCtx.getConfiguration().getProperty(
                    SynapseConstants.PROP_SAL_ENDPOINT_DEFAULT_SESSION_TIMEOUT,
                    SynapseConstants.SAL_ENDPOINTS_DEFAULT_SESSION_TIMEOUT);
        }

        if (log.isDebugEnabled()) {
            log.debug("For session with id " + id + " : expiry time interval : " + expireTimeWindow);
        }

        long expiryTime = System.currentTimeMillis() + expireTimeWindow;

        Endpoint rootEndpoint = endpoints.get(0);

        SessionInformation information = new SessionInformation(id,
                endpoints, expiryTime);

        if (isClustered) {
            List<String> epNameList = getEndpointNames(endpoints);
            information.setPath(epNameList);
            information.setRootEndpointName(getEndpointName(rootEndpoint));
        }
        return information;
    }
    
    /*
     * Factory method to create a session information using given endpoint list, list of paths
     * session id and other informations
     */
    private SessionInformation createSessionInformation(MessageContext synCtx,
                                                        String id, List<Endpoint> endpoints, List<String> paths) {

        if (endpoints == null || endpoints.isEmpty()) {
            handleException("Invalid request to create sessions . Cannot find a endpoint sequence.");
        }

        if (log.isDebugEnabled()) {
            log.debug("Creating a session information for given session id  " + id
                    + " with endpoint sequence " + endpoints);
        }

        long expireTimeWindow = -1;
        assert endpoints != null;
        for (Endpoint endpoint : endpoints) {

            if (endpoint instanceof SALoadbalanceEndpoint) {
                long sessionsTimeout = ((SALoadbalanceEndpoint) endpoint).getSessionTimeout();

                if (expireTimeWindow == -1) {
                    expireTimeWindow = sessionsTimeout;
                } else if (expireTimeWindow > sessionsTimeout) {
                    expireTimeWindow = sessionsTimeout;
                }
            }
        }

        if (expireTimeWindow == -1) {
            expireTimeWindow = synCtx.getConfiguration().getProperty(
                    SynapseConstants.PROP_SAL_ENDPOINT_DEFAULT_SESSION_TIMEOUT,
                    SynapseConstants.SAL_ENDPOINTS_DEFAULT_SESSION_TIMEOUT);
        }

        if (log.isDebugEnabled()) {
            log.debug("For session with id " + id + " : expiry time interval : " + expireTimeWindow);
        }

        long expiryTime = System.currentTimeMillis() + expireTimeWindow;

        Endpoint rootEndpoint = endpoints.get(0);

        SessionInformation information = new SessionInformation(id,
                endpoints, expiryTime);
        information.setPath(paths);

        if (isClustered) {
            information.setRootEndpointName(getEndpointName(rootEndpoint));
        }
        return information;
    }

    /*
     * Factory method to create a session information using a given member node ,
     * session id and other informations
     */
    private SessionInformation createSessionInformation(MessageContext synCtx,
                                                        String id, Member currentMember, List<String> paths) {

        if (currentMember == null) {
            handleException("Invalid request to create sessions.");
        }

        if (log.isDebugEnabled()) {
            log.debug("Creating a session information for given session id  " + id
                    + " with member Host:" + currentMember.getHostName() + " Port:"
                    + currentMember.getPort());
        }

        long expireTimeWindow = synCtx.getConfiguration().getProperty(
                    SynapseConstants.PROP_SAL_ENDPOINT_DEFAULT_SESSION_TIMEOUT,
                    SynapseConstants.SAL_ENDPOINTS_DEFAULT_SESSION_TIMEOUT);

        if (log.isDebugEnabled()) {
            log.debug("For session with id " + id +
                    " : expiry time interval : " + expireTimeWindow);
        }

        long expiryTime = System.currentTimeMillis() + expireTimeWindow;

        SessionInformation sessionInformation = new SessionInformation(id,
                currentMember, expiryTime, expireTimeWindow);
        if (paths != null) {
        	sessionInformation.setPath(paths);
        }
        return sessionInformation;
    }
}
