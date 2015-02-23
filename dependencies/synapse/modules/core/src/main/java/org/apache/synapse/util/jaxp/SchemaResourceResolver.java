package org.apache.synapse.util.jaxp;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.util.resolver.ResourceMap;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.InputSource;

/**
 * External schema resource resolver for Validate Mediator
 * <p/>
 * This will used by Validate mediator to resolve external schema references defined in Validate mediator configuration
 * using <pre> &lt;resource location="location" key="key"/&gt; </pre> inside Validate mediator configuration.
 */
public class SchemaResourceResolver implements LSResourceResolver {

    private ResourceMap resourceMap;
    private SynapseConfiguration synCfg;
    private static final Log log = LogFactory.getLog(SchemaResourceResolver.class);

    public SchemaResourceResolver(SynapseConfiguration synCfg, ResourceMap resourceMap) {
        this.resourceMap = resourceMap;
        this.synCfg = synCfg;
    }

    /**
     * Lookup in {@link org.apache.synapse.util.resolver.ResourceMap} and returns
     * {@link org.apache.synapse.util.jaxp.SchemaResourceLSInput}
     */
    public LSInput resolveResource(String type, String namespaceURI,
                                   String publicId, String systemId,
                                   String baseURI) {

        if (log.isDebugEnabled()) {
            log.debug("Resolving Schema resource " + systemId);
        }

        if (resourceMap == null) {
            log.warn("Unable to resolve schema resource : \"" + systemId +
                    "\". External schema resources not " +
                    "defined in Validate mediator configuration");
            return null;
        }

        InputSource inputSource = resourceMap.resolve(synCfg, systemId);
        if (inputSource == null) {
            log.warn("Unable to resolve schema resource " + systemId);
            return null;
        }
        SchemaResourceLSInput schemaResourceLSInput = new SchemaResourceLSInput();
        schemaResourceLSInput.setByteStream(inputSource.getByteStream());
        return schemaResourceLSInput;
    }
}

