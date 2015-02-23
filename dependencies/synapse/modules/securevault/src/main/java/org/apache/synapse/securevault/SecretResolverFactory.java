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
package org.apache.synapse.securevault;


import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.securevault.secret.SecretCallbackHandler;
import org.apache.synapse.securevault.secret.SecretCallbackHandlerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Factory for creating <code>SecretResolver</code> instances
 */
public class SecretResolverFactory {

    /**
     * Creates an <code>SecretResolver</code> instance from an XML
     *
     * @param configuration <code>SecretResolver</code> configuration as XML object, DOM
     * @param isCapLetter   whether the XML element begins with a cap letter
     * @return an <code>SecretResolver</code> instance
     */
    public static SecretResolver create(Element configuration, boolean isCapLetter) {
        SecretResolver secretResolver = new SecretResolver();
        String secureVaultElementName;
        String passwordProvider = null;
        String secureVaultNamespace = null;
        String secureVaultNamespacePrefix = null;

        if(isCapLetter){
            secureVaultElementName = SecurityConstants.SECURE_VAULT_CAP;
        } else {
            secureVaultElementName = SecurityConstants.SECURE_VAULT_SIMPLE;
        }

        NamedNodeMap nodeMap  = configuration.getAttributes();
        String namespaceUri = null;

        for(int i = 0; i < nodeMap.getLength(); i++){
            String nodeName = nodeMap.item(i).getNodeName();
            secureVaultNamespacePrefix = nodeName.substring(nodeName.
                    indexOf(SecurityConstants.NS_SEPARATOR) + 1);
            namespaceUri  = nodeMap.item(i).getNodeValue();
            if(namespaceUri.startsWith(SecurityConstants.SECURE_VAULT_NS)){
                secureVaultNamespace =  namespaceUri;
                break;
            }
        }

        if(secureVaultNamespace != null){

            NodeList nodeList = configuration.getElementsByTagName(secureVaultNamespacePrefix +
                                                                   SecurityConstants.NS_SEPARATOR +
                                                                   secureVaultElementName);
            if(nodeList.item(0) != null){
                NamedNodeMap namedNodeMap  = nodeList.item(0).getAttributes();
                for(int i = 0; i < namedNodeMap.getLength(); i++){
                    namedNodeMap.item(i).getPrefix();
                    String attributeName = namedNodeMap.item(i).getNodeName();
                    if(attributeName.equals(SecurityConstants.SECURE_VAULT_ATTRIBUTE)){
                        passwordProvider = namedNodeMap.item(i).getNodeValue();
                        break;
                    }
                }
            }
        }

        initPasswordManager(secretResolver, passwordProvider);

        if(secretResolver.isInitialized()){
            addProtectedTokens(configuration, secretResolver, secureVaultNamespacePrefix);
        }

        return secretResolver;
    }

    /**
     * Creates an <code>SecretResolver</code> instance from an XML
     *
     * @param configuration <code>SecretResolver</code> configuration as XML object, OMElement
     * @param isCapLetter   whether the XML element begins with a cap letter
     * @return an <code>SecretResolver</code> instance
     */
    public static SecretResolver create(OMElement configuration, boolean isCapLetter) {

        SecretResolver secretResolver = new SecretResolver();
        String secureVaultElementName;
        String passwordProvider = null;
        OMNamespace secureVaultNamespace = null;

        if(isCapLetter){
            secureVaultElementName = SecurityConstants.SECURE_VAULT_CAP;
        } else {
            secureVaultElementName = SecurityConstants.SECURE_VAULT_SIMPLE;
        }

        Iterator iterator = configuration.getAllDeclaredNamespaces();
        while(iterator.hasNext()){
            OMNamespace omNamespace = (OMNamespace) iterator.next();
            if(omNamespace.getNamespaceURI().startsWith(SecurityConstants.SECURE_VAULT_NS)){
                secureVaultNamespace = omNamespace;
                break;
            }
        }

        if(secureVaultNamespace != null){
            Iterator itr = configuration.
                    getChildrenWithName(new QName(secureVaultNamespace.getNamespaceURI(),                                                  secureVaultElementName, secureVaultNamespace.getPrefix()));
            if(itr.hasNext()){
                passwordProvider = ((OMElement)itr.next()).
                        getAttributeValue(new QName((SecurityConstants.SECURE_VAULT_ATTRIBUTE)));
            }

            initPasswordManager(secretResolver, passwordProvider);

            if(secretResolver.isInitialized()){
                addProtectedTokens(configuration, secureVaultNamespace, secretResolver);
            }
        }
        return secretResolver;
    }



    /**
     * Creates an <code>SecretResolver</code> instance from a set of property
     *
     * @param properties     configuration properties
     * @param propertyPrefix prefix to identify suitable configuration properties
     * @return an <code>SecretResolver</code> instance
     */
    public static SecretResolver create(Properties properties, String propertyPrefix) {

        SecretResolver secretResolver = new SecretResolver();

        String prefix = propertyPrefix;
        if (propertyPrefix != null && !"".equals(propertyPrefix) && !propertyPrefix.endsWith(".")) {
            prefix += ".";
        }
        initPasswordManager(secretResolver, properties.getProperty(prefix +
                SecurityConstants.SECRET_PROVIDER));

        if (secretResolver.isInitialized()) {
            String protectedTokens = properties.getProperty(prefix +
                    SecurityConstants.PROTECTED_TOKENS_SIMPLE);
            if (protectedTokens != null && !"".equals(protectedTokens.trim())) {
                ArrayList<String> tokens = new ArrayList<String>(Arrays
                        .asList(protectedTokens.split(",")));
                for (String token : tokens) {
                    secretResolver.addProtectedToken(token);
                }
            }
        }
        return secretResolver;
    }

    /**
     * Creates an <code>SecretResolver</code> instance from a set of DOM Node
     *
     * @param namedNodeMap DOM node set
     * @return an <code>SecretResolver</code> instance
     */
    public static SecretResolver create(NamedNodeMap namedNodeMap) {

        SecretResolver secretResolver = new SecretResolver();

        Node namedItem = namedNodeMap.getNamedItem(SecurityConstants.PASSWORD_PROVIDER_SIMPLE);
        if (namedItem != null) {
            String passwordProvider = namedItem.getNodeValue();
            if (passwordProvider != null && passwordProvider.trim().length() > 0) {
                initPasswordManager(secretResolver, passwordProvider);
            }
        }

        if (secretResolver.isInitialized()) {
            Node protectedTokenAttr = namedNodeMap.getNamedItem(
                    SecurityConstants.PROTECTED_TOKENS_SIMPLE);
            ArrayList<String> protectedTokenList;
            if (protectedTokenAttr != null) {
                String protectedTokens = protectedTokenAttr.getNodeValue();
                if (protectedTokens != null && protectedTokens.trim().length() > 0) {
                    protectedTokenList = new ArrayList<String>(Arrays.asList(protectedTokens
                            .split(",")));
                    for (String token : protectedTokenList) {
                        if (token != null && !"".equals(token)) {
                            secretResolver.addProtectedToken(token);
                        }
                    }
                }
            }
        }
        return secretResolver;
    }

    private static void initPasswordManager(SecretResolver secretResolver, String provider) {
        SecretCallbackHandler callbackHandler =
                SecretCallbackHandlerFactory.createSecretCallbackHandler(provider);
        if (callbackHandler != null) {
            secretResolver.init(callbackHandler);
        }
    }

    private static void addProtectedTokens(Node node, SecretResolver secretResolver,
                                           String secureVaultNamespacePrefix){
        NodeList nodeList = node.getChildNodes();
        for(int i = 0; i < nodeList.getLength(); i++){
            NamedNodeMap nodeMap = nodeList.item(i).getAttributes();
            if(nodeMap != null){
                for(int j = 0; j < nodeMap.getLength(); j++){
                    String attributeName = nodeMap.item(j).getNodeName();
                    if((secureVaultNamespacePrefix + SecurityConstants.NS_SEPARATOR +
                        SecurityConstants.SECURE_VAULT_ALIAS).equals(attributeName)){
                        secretResolver.addProtectedToken(nodeMap.item(j).getNodeValue());
                    }
                }
            }
            addProtectedTokens(nodeList.item(i), secretResolver, secureVaultNamespacePrefix);
        }
    }

    private static void addProtectedTokens(OMElement configuration, OMNamespace secureVaultNamespace,
                                           SecretResolver secretResolver){

        Iterator iterator = configuration.
                getChildrenWithNamespaceURI(secureVaultNamespace.getNamespaceURI());

        while(iterator.hasNext()){
            OMElement omElement = (OMElement)iterator.next();
            String attributeValue = omElement.
                    getAttributeValue(new QName(secureVaultNamespace.getNamespaceURI(),
                                                SecurityConstants.SECURE_VAULT_ALIAS,
                                                secureVaultNamespace.getPrefix()));

            if(attributeValue != null && !attributeValue.equals("")){
                secretResolver.addProtectedToken(attributeValue);
            }
            addProtectedTokens(omElement, secureVaultNamespace, secretResolver);
        }
    }
}
