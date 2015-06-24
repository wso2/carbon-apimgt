package org.apache.synapse.util.xpath;

import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.jaxen.UnresolvableException;
import org.jaxen.VariableContext;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;

public class DOMSynapseXPathVariableResolver implements XPathVariableResolver {

    MessageContext synCtx;
    VariableContext parentVariableContext;

    public DOMSynapseXPathVariableResolver(VariableContext parentVariableContext, MessageContext synCtx) {
        this.parentVariableContext = parentVariableContext;
        this.synCtx = synCtx;
    }

    public Object resolveVariable(QName variable) {

        try {
            SynapseXPathVariableContext variableContext = new SynapseXPathVariableContext(
                    parentVariableContext, synCtx);
            return variableContext.getVariableValue(SynapseConstants.SYNAPSE_NAMESPACE.equals(
                    variable.getNamespaceURI())?null:variable.getNamespaceURI()
                    , variable.getPrefix(), variable.getLocalPart());
        } catch (UnresolvableException e) {
            throw new SynapseException("DOM Synapse XPATH variable resolution failed",e);
        }
    }
}
