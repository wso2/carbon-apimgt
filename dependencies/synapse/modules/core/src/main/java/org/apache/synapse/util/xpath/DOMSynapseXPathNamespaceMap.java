package org.apache.synapse.util.xpath;

import org.apache.synapse.SynapseConstants;

import java.util.*;

import javax.xml.namespace.NamespaceContext;

public class DOMSynapseXPathNamespaceMap implements NamespaceContext {
    Map<String, String> prefixToURIMap = new HashMap<String, String>();
    Set<String> knownPrefixMap = new HashSet<String>();

    public DOMSynapseXPathNamespaceMap() {
        // Adding known prefixes to synapse namespace
        knownPrefixMap.add("syn");
        knownPrefixMap.add(SynapseXPathConstants.SOAP_HEADER_VARIABLE);
        knownPrefixMap.add(SynapseXPathConstants.SOAP_BODY_VARIABLE);
        knownPrefixMap.add(SynapseXPathConstants.FUNC_CONTEXT_VARIABLE_PREFIX);
        knownPrefixMap.add(SynapseXPathConstants.MESSAGE_CONTEXT_VARIABLE_PREFIX);
        knownPrefixMap.add(SynapseXPathConstants.URL_VARIABLE_PREFIX);
        knownPrefixMap.add(SynapseXPathConstants.AXIS2_CONTEXT_VARIABLE_PREFIX);
        knownPrefixMap.add(SynapseXPathConstants.TRANSPORT_VARIABLE_PREFIX);
    }

    public String getNamespaceURI(String prefix) {
        if (knownPrefixMap.contains(prefix)) {
            return SynapseConstants.SYNAPSE_NAMESPACE;
        } else {
            return prefixToURIMap.get(prefix);
        }
    }

    public String getPrefix(String namespaceURI) {
        return null;
    }

    public Iterator getPrefixes(String namespaceURI) {
        return null;
    }

    public void addNamespace(String prefix, String uri) {
        prefixToURIMap.put(prefix, uri);
    }
}
