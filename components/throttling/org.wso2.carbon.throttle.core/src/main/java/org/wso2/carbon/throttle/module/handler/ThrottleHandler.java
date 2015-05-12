/*
* Copyright 2005,2006 WSO2, Inc. http://wso2.com
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
*
*/

package org.wso2.carbon.throttle.module.handler;


import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.throttle.core.*;
import org.wso2.carbon.throttle.module.utils.StatCollector;
import org.wso2.carbon.throttle.module.utils.impl.DummyAuthenticator;
import org.wso2.carbon.throttle.module.utils.impl.DummyHandler;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import java.util.Map;


public abstract class ThrottleHandler extends AbstractHandler {

    private static Log log = LogFactory.getLog(ThrottleHandler.class.getName());
    /* The AccessRateController - control(limit) access for a remote caller */
    private AccessRateController accessRateController;

    private RoleBasedAccessRateController roleBasedAccessController;

    public static final String THROTTLING_CACHE_MANAGER = "throttling.cache.manager";

    public static final String THROTTLING_CACHE = "throttling.cache";

    private boolean debugOn;

    public ThrottleHandler() {
        this.debugOn = log.isDebugEnabled();
        this.accessRateController = new AccessRateController();
        this.roleBasedAccessController = new RoleBasedAccessRateController();
    }

    /**
     * @return int - indicates the type of the throttle according to the scope
     */
    protected abstract int getThrottleType();

    /**
     * Loads a throttle metadata for a particular throttle type
     *
     * @param messageContext - The messageContext
     * @param throttleType   - The type of throttle
     * @return IPBaseThrottleConfiguration     - The IPBaseThrottleConfiguration - load from AxisConfiguration
     * @throws ThrottleException Throws if the throttle type is unsupported
     */

    public Throttle loadThrottle(MessageContext messageContext,
                                 int throttleType) throws ThrottleException {

        Throttle throttle = null;
        ConfigurationContext configContext = messageContext.getConfigurationContext();
        //the Parameter which hold throttle ipbase object
        // to get throttles map from the configuration context

        Map throttles = (Map) configContext.getPropertyNonReplicable(ThrottleConstants.THROTTLES_MAP);
        if (throttles == null) {
            if (debugOn) {
                log.debug("Couldn't find throttles object map .. thottlling will not be occurred ");
            }
            return null;
        }
        switch (throttleType) {
            case ThrottleConstants.GLOBAL_THROTTLE: {
                throttle =
                        (Throttle) throttles.get(ThrottleConstants.GLOBAL_THROTTLE_KEY);
                break;
            }
            case ThrottleConstants.OPERATION_BASED_THROTTLE: {
                AxisOperation axisOperation = messageContext.getAxisOperation();
                if (axisOperation != null) {
                    QName opName = axisOperation.getName();
                    if (opName != null) {
                        AxisService service = (AxisService) axisOperation.getParent();
                        if (service != null) {
                            String currentServiceName = service.getName();
                            if (currentServiceName != null) {
                                throttle =
                                        (Throttle) throttles.get(currentServiceName +
                                                opName.getLocalPart());
                            }
                        }
                    }
                } else {
                    if (debugOn) {
                        log.debug("Couldn't find axis operation ");
                    }
                    return null;
                }
                break;
            }
            case ThrottleConstants.SERVICE_BASED_THROTTLE: {
                AxisService axisService = messageContext.getAxisService();
                if (axisService != null) {
                    throttle =
                            (Throttle) throttles.get(axisService.getName());
                } else {
                    if (debugOn) {
                        log.debug("Couldn't find axis service ");
                    }
                    return null;
                }
                break;
            }
            default: {
                throw new ThrottleException("Unsupported Throttle type");
            }
        }
        return throttle;
    }

    /**
     * processing through the throttle
     * 1) concurrent throttling
     * 2) access rate based throttling - domain or ip
     *
     * @param throttle       The Throttle object - holds all configuration and state data
     *                       of the throttle
     * @param messageContext The MessageContext , that holds all data per message basis
     * @throws AxisFault         Throws when access must deny for caller
     * @throws ThrottleException    ThrottleException
     */
    public void process(Throttle throttle,
                        MessageContext messageContext) throws ThrottleException, AxisFault {

        String throttleId = throttle.getId();
        ConfigurationContext cc = messageContext.getConfigurationContext();

        // acquiring  cache manager.
        Cache<String, ConcurrentAccessController> cache;
        CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager(THROTTLING_CACHE_MANAGER);
        if (cacheManager != null) {
            cache = cacheManager.getCache(THROTTLING_CACHE);
        } else {
            cache = Caching.getCacheManager().getCache(THROTTLING_CACHE);
        }
        if (log.isDebugEnabled()) {
            log.debug("created throttling cache : " + cache);
        }
        // Get the concurrent access controller
        ConcurrentAccessController cac;
        String key = null;
        key = ThrottleConstants.THROTTLE_PROPERTY_PREFIX + throttleId
                + ThrottleConstants.CAC_SUFFIX;
        cac = cache.get(key);

        // check for concurrent access
        boolean canAccess = doConcurrentThrottling(cac, messageContext);

        if (canAccess) {
            // if the concurrent access is success then
            // do the access rate based throttling

            if (messageContext.getFLOW() == MessageContext.IN_FLOW) {
                //gets the remote caller domain name
                String domain = null;
                HttpServletRequest request =
                        (HttpServletRequest) messageContext.getPropertyNonReplicable(
                                HTTPConstants.MC_HTTP_SERVLETREQUEST);
                if (request != null) {
                    domain = request.getRemoteHost();
                }

                // Domain name based throttling
                //check whether a configuration has been defined for this domain name or not
                String callerId = null;
                if (domain != null) {
                    //loads the ThrottleContext
                    ThrottleContext throttleCtxt =
                            throttle.getThrottleContext(ThrottleConstants.DOMAIN_BASED_THROTTLE_KEY);
                    if (throttleCtxt != null) {
                        //Loads the ThrottleConfiguration
                        ThrottleConfiguration throttleConfig = throttleCtxt.getThrottleConfiguration();
                        if (throttleConfig != null) {
                            //check for configuration for this caller
                            callerId = throttleConfig.getConfigurationKeyOfCaller(domain);
                            if (callerId != null) {
                                // If this is a clustered env.
                                throttleCtxt.setThrottleId(throttleId);
                                AccessInformation infor =
                                        accessRateController.canAccess(throttleCtxt, callerId,
                                                ThrottleConstants.DOMAIN_BASE);
                                StatCollector.collect(infor, domain, ThrottleConstants.DOMAIN_BASE);

                                //check for the permission for access
                                if (!infor.isAccessAllowed()) {

                                    //In the case of both of concurrency throttling and
                                    //rate based throttling have enabled ,
                                    //if the access rate less than maximum concurrent access ,
                                    //then it is possible to occur death situation.To avoid that reset,
                                    //if the access has denied by rate based throttling
                                    if (cac != null) {
                                        cac.incrementAndGet();
                                        cache.put(key, cac);
                                        if (debugOn) {
                                            log.debug("Added the state of ConcurrentAccessController " +
                                                    "to cache with key : " + key);
                                        }
                                    }
                                    throw new AxisFault(" Access deny for a " +
                                            "caller with Domain " + domain + " " +
                                            " : Reason : " + infor.getFaultReason());
                                }
                            } else {
                                if (debugOn) {
                                    log.debug("Could not find the Throttle Context for domain-Based " +
                                            "Throttling for domain name " + domain + " Throttling for this " +
                                            "domain name may not be configured from policy");
                                }
                            }
                        }
                    }
                } else {
                    if (debugOn) {
                        log.debug("Could not find the domain of the caller - IP-based throttling may occur");
                    }
                }

                //IP based throttling - Only if there is no configuration for caller domain name

                if (callerId == null) {
                    String ip = (String) messageContext.getProperty(MessageContext.REMOTE_ADDR);
                    if (ip != null) {
                        // loads IP based throttle context
                        ThrottleContext context =
                                throttle.getThrottleContext(ThrottleConstants.IP_BASED_THROTTLE_KEY);
                        if (context != null) {
                            //Loads the ThrottleConfiguration
                            ThrottleConfiguration config = context.getThrottleConfiguration();
                            if (config != null) {
                                // check for configuration for this ip
                                callerId = config.getConfigurationKeyOfCaller(ip);
                                if (callerId != null) {
                                        context.setThrottleId(throttleId);
                                    AccessInformation infor =
                                            accessRateController.canAccess(context, callerId,
                                                    ThrottleConstants.IP_BASE);
                                    // check for the permission for access
                                    StatCollector.collect(infor,ip,ThrottleConstants.IP_BASE);
                                    if (!infor.isAccessAllowed()) {

                                        //In the case of both of concurrency throttling and
                                        //rate based throttling have enabled ,
                                        //if the access rate less than maximum concurrent access ,
                                        //then it is possible to occur death situation.To avoid that reset,
                                        //if the access has denied by rate based throttling
                                        if (cac != null) {
                                            cac.incrementAndGet();
                                            // set back if this is a clustered env
                                                cache.put(key, cac);
                                                if(debugOn) {
                                                    log.debug("Added the state of ConcurrentAccessController " +
                                                            "to cache with key : " + key);
                                            }
                                        }
                                        throw new AxisFault(" Access deny for a " +
                                                "caller with IP " + ip + " " +
                                                " : Reason : " + infor.getFaultReason());
                                    }
                                }
                            }
                        } else {
                            if (debugOn) {
                                log.debug("Could not find the throttle Context for IP-Based throttling");
                            }
                        }
                    } else {
                        if (debugOn) {
                            log.debug("Could not find the IP address of the caller " +
                                    "- throttling will not occur");
                        }
                    }
                }
            }
            // all the replication functionality of the access rate based throttling handles by itself
            // just replicate the current state of ConcurrentAccessController
            if (cac != null) {
                cache.put(key, cac);
                if(debugOn) {
                    log.debug("Added the state of ConcurrentAccessController " +
                            "to cache with key : " + key);
                }
            }

            //finally engage rolebased access throttling if available
            doRoleBasedAccessThrottling(throttle, messageContext);
        } else {
            //replicate the current state of ConcurrentAccessController
            if (cac != null) {
                cache.put(key, cac);
                if(debugOn) {
                    log.debug("Added the state of ConcurrentAccessController " +
                            "to cache with key : " + key);
                }
            }
            throw new AxisFault("Access has currently been denied since " +
                    " maximum concurrent access have exceeded");
        }

    }

    /**
     * Helper method for handling concurrent throttling
     *
     * @param concurrentAccessController ConcurrentAccessController
     * @param messageContext             MessageContext - message level states
     * @return true if access is allowed through concurrent throttling ,o.w false
     */
    private boolean doConcurrentThrottling(ConcurrentAccessController concurrentAccessController, MessageContext messageContext) {

        boolean canAccess = true;
        int available;

        if (concurrentAccessController != null) {
            if (messageContext.getFLOW() == MessageContext.IN_FLOW) {
                available = concurrentAccessController.getAndDecrement();
                canAccess = available > 0;
                if (debugOn) {
                    log.debug("Concurrency Throttle : Access " + (canAccess ? "allowed" : "denied") +
                            " :: " + available + " of available of " +
                            concurrentAccessController.getLimit() + " connections");
                }
                if (debugOn) {
                    if (!canAccess) {
                        log.debug("Concurrency Throttle : Access has currently been denied since allowed" +
                                " maximum concurrent access have exceeded");
                    }
                }
            } else if (messageContext.getFLOW() == MessageContext.OUT_FLOW) {
                available = concurrentAccessController.incrementAndGet();
                if (debugOn) {
                    log.debug("Concurrency Throttle : Connection returned" +
                            " :: " + available + " of available of "
                            + concurrentAccessController.getLimit() + " connections");
                }
            }
        }
        return canAccess;
    }


    /**
     * Helper method for handling role based Access throttling
     *
     * @param messageContext             MessageContext - message level states
     * @return true if access is allowed through concurrent throttling ,o.w false
     */
    private boolean doRoleBasedAccessThrottling(Throttle throttle, MessageContext messageContext) throws
            AxisFault,ThrottleException {

        boolean canAccess = true;
        if (throttle.getThrottleContext(ThrottleConstants.ROLE_BASED_THROTTLE_KEY) == null) {
            //if no role base throttle config return immediately
            return canAccess;
        }
        ConfigurationContext cc = messageContext.getConfigurationContext();
        String throttleId = throttle.getId();

        // acquiring  cache manager.
        Cache<String, ConcurrentAccessController> cache;
        CacheManager cacheManager = Caching.getCacheManagerFactory().getCacheManager(THROTTLING_CACHE_MANAGER);
        if (cacheManager != null) {
            cache = cacheManager.getCache(THROTTLING_CACHE);
        } else {
            cache = Caching.getCacheManager().getCache(THROTTLING_CACHE);
        }
        if (log.isDebugEnabled()) {
            log.debug("created throttling cache : " + cache);
        }
        String key = null;
        ConcurrentAccessController cac = null;
        key = ThrottleConstants.THROTTLE_PROPERTY_PREFIX + throttleId
                + ThrottleConstants.CAC_SUFFIX;
        cac = cache.get(key);

        if (messageContext.getFLOW() == MessageContext.IN_FLOW) {
            //gets the remote caller role name
            String consumerKey = null;
            boolean isAuthenticated = false;
            String roleID = null;
            HttpServletRequest request =
                    (HttpServletRequest) messageContext.getPropertyNonReplicable(
                            HTTPConstants.MC_HTTP_SERVLETREQUEST);
            if (request != null) {
                String oAuthHeader = request.getHeader("OAuth");
//                consumerKey = Utils.extractCustomerKeyFromAuthHeader(oAuthHeader);
//                roleID = Utils.extractCustomerKeyFromAuthHeader(oAuthHeader);
                DummyAuthenticator authFuture = new DummyAuthenticator(oAuthHeader);
                consumerKey = authFuture.getAPIKey();
                new DummyHandler().authenticateUser(authFuture);
                roleID = (String) authFuture.getAuthorizedRoles().get(0);
                isAuthenticated = authFuture.isAuthenticated();
            }

            if(!isAuthenticated){
                throw new AxisFault(" Access deny for a " +
                        "caller with consumer Key: " + consumerKey + " " +
                        " : Reason : Authentication failure");

            }
            // Domain name based throttling
            //check whether a configuration has been defined for this role name or not
            String consumerRoleID = null;
            if (consumerKey != null && isAuthenticated) {
                //loads the ThrottleContext
                ThrottleContext context =
                        throttle.getThrottleContext(ThrottleConstants.ROLE_BASED_THROTTLE_KEY);
                if (context != null) {
                    //Loads the ThrottleConfiguration
                    ThrottleConfiguration config = context.getThrottleConfiguration();
                    if (config != null) {
                        //check for configuration for this caller
                        consumerRoleID = config.getConfigurationKeyOfCaller(roleID);
                        if (consumerRoleID != null) {
                            context.setThrottleId(throttleId);
                            AccessInformation infor =
                                    roleBasedAccessController.canAccess(context, consumerKey,
                                            consumerRoleID);
                            StatCollector.collect(infor, consumerKey, ThrottleConstants.ROLE_BASE);
                            //check for the permission for access
                            if (!infor.isAccessAllowed()) {

                                //In the case of both of concurrency throttling and
                                //rate based throttling have enabled ,
                                //if the access rate less than maximum concurrent access ,
                                //then it is possible to occur death situation.To avoid that reset,
                                //if the access has denied by rate based throttling
                                if (cac != null) {
                                    cac.incrementAndGet();

                                            cache.put(key, cac);
                                            if(debugOn) {
                                                log.debug("Added the state of ConcurrentAccessController " +
                                                        "to cache with key : " + key);
                                            }
                                }
                                throw new AxisFault(" Access deny for a " +
                                        "caller with Domain " + consumerKey + " " +
                                        " : Reason : " + infor.getFaultReason());
                            }
                        } else {
                            if (debugOn) {
                                log.debug("Could not find the Throttle Context for role-Based " +
                                        "Throttling for role name " + consumerKey + " Throttling for this " +
                                        "role name may not be configured from policy");
                            }
                        }
                    }
                }
            } else {
                if (debugOn) {
                    log.debug("Could not find the role of the caller - role based throttling NOT applied");
                }
            }
        }
        return canAccess;
    }

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        //Load throttle
        try {
            Throttle throttle = loadThrottle(msgContext, getThrottleType());
            if (throttle != null) {
                process(throttle, msgContext);
            }
        } catch (ThrottleException e) {
            log.error(e.getMessage());
            throw new AxisFault(e.getMessage());
        }
        return InvocationResponse.CONTINUE;
    }

}
