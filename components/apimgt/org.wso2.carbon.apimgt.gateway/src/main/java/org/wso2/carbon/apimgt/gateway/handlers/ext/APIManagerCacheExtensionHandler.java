package org.wso2.carbon.apimgt.gateway.handlers.ext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.cache.Caching;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

/**
 * A simple extension handler to clear cache entries associated with the token when token revoked and refreshed.
 * /revoke api is there to revoke access tokens and it will call to key management server's oauth component and
 * revokes token and clear oauth cache. But there can be validation information objects
 * cached at gateway which associated with that token. So this handler will remove them form the cache.
 */
public class APIManagerCacheExtensionHandler extends AbstractHandler {

    private static final String EXT_SEQUENCE_PREFIX = "WSO2AM--Ext--";
    private static final String DIRECTION_IN = "In";
    private static final String DIRECTION_OUT = "Out";
    private static final Log log = LogFactory.getLog(APIManagerCacheExtensionHandler.class);

    public boolean mediate(MessageContext messageContext, String direction) {
        // In order to avoid a remote registry call occurring on each invocation, we
        // directly get the extension sequences from the local registry.
        Map localRegistry = messageContext.getConfiguration().getLocalRegistry();

        Object sequence = localRegistry.get(EXT_SEQUENCE_PREFIX + direction);
        if (sequence != null && sequence instanceof Mediator) {
            if (!((Mediator) sequence).mediate(messageContext)) {
                return false;
            }
        }

        String apiName = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
        sequence = localRegistry.get(apiName + "--" + direction);
        if (sequence != null && sequence instanceof Mediator) {
            return ((Mediator) sequence).mediate(messageContext);
        }
        return true;
    }

    private void clearCacheForAccessToken(MessageContext messageContext) {
        
        org.apache.axis2.context.MessageContext axisMC = ((Axis2MessageContext) messageContext).getAxis2MessageContext();
        try {
            String revokedToken = (String) ((TreeMap) axisMC.getProperty("TRANSPORT_HEADERS")).get("RevokedAccessToken");
            String renewedToken = (String) ((TreeMap) axisMC.getProperty("TRANSPORT_HEADERS")).get("DeactivatedAccessToken");
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME, true);
            Iterator iterator = Caching.getCacheManager("API_MANAGER_CACHE").getCache("keyCache").keys();
            while (iterator.hasNext()) {
                String cacheKey = iterator.next().toString();
                if (revokedToken != null && cacheKey.contains(revokedToken)) {
                    Caching.getCacheManager("API_MANAGER_CACHE").getCache("keyCache").remove(cacheKey);
                    break;
                } else if (renewedToken != null && cacheKey.contains(renewedToken)) {
                    Caching.getCacheManager("API_MANAGER_CACHE").getCache("keyCache").remove(cacheKey);
                    break;
                }
            }

            // APIKeyValidationInfoDTO info = (APIKeyValidationInfoDTO) getKeyCache().get(cacheKey);
            if (revokedToken != null) {
                if (log.isDebugEnabled()) {
                    log.debug("clearing cache entries associated with token " + revokedToken);
                }
            }
        } catch (Exception e) {
            log.error("Error while clearing cache");
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }
    
    public boolean handleRequest(MessageContext messageContext) {
        return true;
    }

    public boolean handleResponse(MessageContext messageContext) {
        clearCacheForAccessToken(messageContext);
        return mediate(messageContext, DIRECTION_OUT);
    }
}