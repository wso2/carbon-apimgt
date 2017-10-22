package org.wso2.carbon.apimgt.gateway.handlers.throttling;

import edu.umd.cs.findbugs.annotations.*;
import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.OMDocumentImpl;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.PolicyEngine;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.commons.throttle.core.Throttle;
import org.apache.synapse.commons.throttle.core.ThrottleConstants;
import org.apache.synapse.commons.throttle.core.ThrottleContext;
import org.apache.synapse.commons.throttle.core.ThrottleDataHolder;
import org.apache.synapse.commons.throttle.core.ThrottleException;
import org.apache.synapse.commons.throttle.core.ThrottleFactory;
import org.apache.synapse.config.Entry;
import org.apache.synapse.util.SynapseBinaryDataSource;
import org.wso2.carbon.apimgt.gateway.handlers.security.APISecurityUtils;
import org.wso2.carbon.apimgt.gateway.handlers.security.AuthenticationContext;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.mediation.registry.RegistryExtension;
import org.wso2.carbon.mediation.registry.RegistryServiceHolder;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.service.RegistryService;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.activation.DataHandler;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class ApplicationThrottleController {
    
    public static final String APP_THROTTLE_CONTEXT_PREFIX = "APP_THROTTLE_CONTEXT_";

    public static final String GOVERNANCE_REGISTRY_PREFIX = "gov:";

    private static final Log log = LogFactory.getLog(ApplicationThrottleController.class);

    private static final Object lock = new Object();

    public static ThrottleContext getApplicationThrottleContext(MessageContext synCtx, ThrottleDataHolder dataHolder,
            String applicationId, String policyKeyApplication) {
        synchronized (lock) {
            Object throttleContext = dataHolder.getThrottleContext(applicationId);
            if(throttleContext == null){
                return createThrottleContext(synCtx, dataHolder, applicationId, policyKeyApplication);
            }
            return (ThrottleContext)throttleContext;
        }
    }

    private static ThrottleContext createThrottleContext(MessageContext synCtx, ThrottleDataHolder dataHolder,
            String applicationId, String policyKeyApplication) {

        //Object entryValue = synCtx.getEntry(APPLICATION_THROTTLE_POLICY_KEY);
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        RealmService realmService = (RealmService) carbonContext.getOSGiService(RealmService.class, null);

        AuthenticationContext authContext = APISecurityUtils.getAuthenticationContext(synCtx);
        //extract the subscriber username from the auth Context
        String subscriber = authContext.getSubscriber();

        //get the tenant Domain from the subscriber
        String tenantDomain = MultitenantUtils.getTenantDomain(subscriber);
        int tenantId;

        //get the tenant domain id from the tenant domain name
        try {
            tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            handleException("Unable to Find the tenant ID using tenant: " + tenantDomain, e);
            return null;
        }

        Object entryValue = lookup(policyKeyApplication, tenantId);
        if (entryValue == null || !(entryValue instanceof OMElement)) {
            handleException("Unable to load throttling policy using key: " + policyKeyApplication);
        }

        try {
            Throttle throttle = ThrottleFactory.createMediatorThrottle(PolicyEngine.getPolicy((OMElement) entryValue));
            ThrottleContext context = throttle.getThrottleContext(ThrottleConstants.ROLE_BASED_THROTTLE_KEY);
            dataHolder.addThrottleContext(applicationId, context);
            return context;
        } catch (ThrottleException e) {
            handleException("Error processing the throttling policy", e);
        }
        return null;
    }

    private static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    private static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }
    
    private static OMNode lookup(String key, int tenantId) {
        try {
            Resource resource = getResource(key, tenantId);

            if (resource instanceof Collection || resource == null) {
                return null;
            }

            ByteArrayInputStream inputStream = null;
            Object content = resource.getContent();
            if (content instanceof String) {
                inputStream = new ByteArrayInputStream(content.toString().getBytes(Charset.defaultCharset()));
            } else if (content instanceof byte[]) {
                inputStream = new ByteArrayInputStream((byte[]) content);
            }

            OMNode result = null;
            try {
                XMLInputFactory factory = XMLInputFactory.newInstance();
                factory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
                XMLStreamReader parser = factory.createXMLStreamReader(inputStream);

                StAXOMBuilder builder = new StAXOMBuilder(parser);
                result = builder.getDocumentElement();

            } catch (OMException ignored) {
                result = readNonXML(resource);

            } catch (XMLStreamException ignored) {
                result = readNonXML(resource);

            } catch (Exception e) {
                // a more general exception(e.g. a Runtime exception if the XML doc has an
                // external DTD deceleration and if not connected to internet) which in case
                // just log for debugging
                log.error("Error while reading the resource '" + key + '\'', e);
            } finally {
                try {
                    resource.discard();
                    if (result != null && result.getParent() != null) {
                        result.detach();
                        OMDocumentImpl parent = new OMDocumentImpl(OMAbstractFactory.getOMFactory());
                        parent.addChild(result);
                    }
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (IOException e) {
                    log.error("Error while closing the input stream", e);
                }
            }
            return result;

        } catch (RegistryException e) {
            handleException("Error while fetching the resource " + key, e);
        }

        return null;
    }

    private static OMNode readNonXML(Resource resource) throws RegistryException {

        if (log.isDebugEnabled()) {
            log.debug("The resource at the specified path does not contain " +
                    "well-formed XML - Processing as text");
        }

        if (resource != null) {
            if ("text/plain".equals(resource.getMediaType())) {
                // for non-xml text content
                return OMAbstractFactory.getOMFactory().createOMText(
                        new String((byte[]) resource.getContent(),Charset.defaultCharset()));
            }

            ByteArrayInputStream inputStream = new ByteArrayInputStream(
                    (byte[]) resource.getContent());
            try {
                OMFactory omFactory = OMAbstractFactory.getOMFactory();
                return omFactory.createOMText(
                        new DataHandler(new SynapseBinaryDataSource(inputStream,
                                resource.getMediaType())), true);
            } catch (IOException e) {
                handleException("Error while getting a stream from resource content ", e);
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("Error while closing the input stream", e);
                }
            }
        }
        return null;
    }

    private static Resource getResource(String path, int tenantId) {

        RegistryService registryService = RegistryServiceHolder.getInstance().getRegistryService();

        Registry registry = null;
        try {
            registry = registryService.getGovernanceSystemRegistry(tenantId);
        } catch (RegistryException e) {
            log.error("Error while fetching Governance Registry of Super Tenant", e);
            return null;
        }

        String key = resolvePath(path);

        try {
            if (registry.resourceExists(key)) {
                return registry.get(key);
            }
        } catch (RegistryException e) {
            handleException("Error while fetching the resource " + path, e);
        }
        return null;
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings (value = "UCPM_USE_CHARACTER_PARAMETERIZED_METHOD",
            justification = "The error occurs due to FB violation in Registry code")
    private static String resolvePath(String path) {
        if (path == null || "".equals(path)) {
            return RegistryConstants.ROOT_PATH;
        }

        path = path.substring(GOVERNANCE_REGISTRY_PREFIX.length());

        if (path.startsWith(RegistryConstants.PATH_SEPARATOR)) {
                path = path.substring(1);
        }
        return RegistryConstants.ROOT_PATH + path;
    }
}
