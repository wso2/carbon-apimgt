package org.apache.synapse.util.xpath;

import org.apache.synapse.MessageContext;
import org.apache.synapse.mediators.GetPropertyFunction;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathFunction;
import javax.xml.xpath.XPathFunctionResolver;

public class GetPropertyFunctionResolver implements XPathFunctionResolver {

    private MessageContext synCtx;
    public GetPropertyFunctionResolver(MessageContext synCtx) {
        this.synCtx = synCtx;
    }

    public XPathFunction resolveFunction(QName functionName, int arity) {
        if (SynapseXPathConstants.GET_PROPERTY_FUNCTION.equals(functionName.getLocalPart())) {
            return new GetPropertyFunction(synCtx);
        }
        return null;
    }
}
