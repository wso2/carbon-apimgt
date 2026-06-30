/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.mediators;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMException;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.transport.passthru.util.RelayUtils;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.threatprotection.APIMThreatAnalyzerException;
import org.wso2.carbon.apimgt.gateway.threatprotection.AnalyzerHolder;
import org.wso2.carbon.apimgt.gateway.threatprotection.analyzer.APIMThreatAnalyzer;
import org.wso2.carbon.apimgt.gateway.threatprotection.configuration.XMLConfig;
import org.wso2.carbon.apimgt.gateway.threatprotection.utils.ThreatExceptionHandler;
import org.wso2.carbon.apimgt.gateway.threatprotection.utils.ThreatProtectorConstants;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.gateway.utils.GatewayUtils;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * This mediator would protect the backend resources from the XML threat vulnerabilities by validating the
 * XML schema.
 */
public class XMLSchemaValidator extends AbstractMediator {
    private static final Log logger = LogFactory.getLog(XMLSchemaValidator.class);
    private static final String APPLICATION_BUILDER_ALLOW_DTD = "ApplicationXMLBuilder.allowDTD";
    APIManagerConfiguration apiManagerConfiguration;
    boolean isSecureXMLProcessingEnabled = true;

    /**
     * This mediate method validates the xml request message.
     *
     * @param messageContext This message context contains the request message properties of the relevant
     *                       API which was enabled the XML_Validator message mediation in flow.
     * @return A boolean value.True if successful and false if not.
     */
    public boolean mediate(MessageContext messageContext) {
        InputStream inputStreamSchema;
        InputStream inputStreamXml;
        Boolean xmlValidationStatus;
        Boolean schemaValidationStatus;
        APIMThreatAnalyzer apimThreatAnalyzer = null;
        String apiContext;
        String requestMethod;
        String contentType = "";
        boolean isValid = true;
        apiManagerConfiguration = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfiguration();
        if (apiManagerConfiguration != null) {
            isSecureXMLProcessingEnabled = apiManagerConfiguration.isEnableSecureXMLProcessing();
        }
        if (isSecureXMLProcessingEnabled) {
            logger.debug("Secure XML processing is enabled, disallowing DTD processing");
            ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                    .setProperty(APPLICATION_BUILDER_ALLOW_DTD, "false");
        } else {
             ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                     .setProperty(APPLICATION_BUILDER_ALLOW_DTD, "true");
         }
        org.apache.axis2.context.MessageContext axis2MC = ((Axis2MessageContext) messageContext).
                getAxis2MessageContext();
        requestMethod = axis2MC.getProperty(ThreatProtectorConstants.HTTP_REQUEST_METHOD).toString();
        Object contentTypeObject = axis2MC.getProperty(ThreatProtectorConstants.CONTENT_TYPE);
        if (contentTypeObject != null) {
            contentType = contentTypeObject.toString();
        } else {
            Object contentTypeProperty = axis2MC.getProperty(ThreatProtectorConstants.SOAP_CONTENT_TYPE);
            if (contentTypeProperty != null) {
                contentType = contentTypeProperty.toString();
            }
        }
        apiContext = messageContext.getProperty(ThreatProtectorConstants.API_CONTEXT).toString();
        if (logger.isDebugEnabled()) {
            logger.debug("XML schema validation mediator is activated... API Context: "
                    + apiContext + ", Request Method: " + requestMethod);
        }
        if (!APIConstants.SupportedHTTPVerbs.GET.name().equalsIgnoreCase(requestMethod) &&
                (ThreatProtectorConstants.APPLICATION_XML.equals(contentType) ||
                        ThreatProtectorConstants.TEXT_XML.equals(contentType))) {
            try {
                String payload = extractPayload(axis2MC);
                Object messageProperty = messageContext.getProperty(APIMgtGatewayConstants.XML_VALIDATION);
                if (messageProperty != null) {
                    xmlValidationStatus = Boolean.valueOf(messageProperty.toString());
                    if (xmlValidationStatus.equals(true)) {
                        XMLConfig xmlConfig = configureSchemaProperties(messageContext);
                        apimThreatAnalyzer = AnalyzerHolder.getAnalyzer(contentType);
                        apimThreatAnalyzer.configure(xmlConfig);
                        inputStreamXml = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
                        apimThreatAnalyzer.analyze(inputStreamXml, apiContext);
                    }
                }
                messageProperty = messageContext.getProperty(APIMgtGatewayConstants.SCHEMA_VALIDATION);
                if (messageProperty != null) {
                    schemaValidationStatus = Boolean.valueOf(messageProperty.toString());
                    if (schemaValidationStatus.equals(true)) {
                        inputStreamSchema = new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8));
                        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStreamSchema);
                        validateSchema(messageContext, bufferedInputStream);
                    }
                }
            } catch (APIMThreatAnalyzerException e) {
                logger.error(APIMgtGatewayConstants.BAD_REQUEST, e);
                isValid = GatewayUtils.handleThreat(messageContext, ThreatProtectorConstants.HTTP_SC_CODE, e.getMessage());

            } catch (IOException | XMLStreamException e) {
                logger.error(APIMgtGatewayConstants.BAD_REQUEST, e);
                isValid = GatewayUtils.handleThreat(messageContext, APIMgtGatewayConstants.HTTP_SC_CODE, e.getMessage());
            } finally {
                // return analyzer to the pool
                if (apimThreatAnalyzer != null) {
                    AnalyzerHolder.returnObject(apimThreatAnalyzer);
                }
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("XML Schema Validator: " + APIMgtGatewayConstants.REQUEST_TYPE_FAIL_MSG);
            }
        }
        return isValid;
    }

    /**
     * This method binds the properties of the json validator sequence with the XMLConfig object.
     *
     * @param messageContext This message context contains the request message properties of the relevant
     *                       API which was enabled the XML_Validator message mediation in flow.
     * @return XMLConfig contains the xml schema properties need to be validated.
     */
    XMLConfig configureSchemaProperties(MessageContext messageContext) {
        Object messageProperty;
        boolean dtdEnabled = false;
        boolean externalEntitiesEnabled = false;
        int maxXMLDepth = 0;
        int elementCount = 0;
        int attributeLength = 0;
        int attributeCount = 0;
        int entityExpansionLimit = 0;
        int childrenPerElement = 0;

        messageProperty = messageContext.getProperty(ThreatProtectorConstants.DTD_ENABLED);
        if (messageProperty != null) {
            dtdEnabled = Boolean.valueOf(messageProperty.toString());
        } else {
            String message = "XML schema dtdEnabled property value is missing.";
            ThreatExceptionHandler.handleException(messageContext, message);
        }

        messageProperty = messageContext.getProperty(ThreatProtectorConstants.EXTERNAL_ENTITIES_ENABLED);
        if (messageProperty != null) {
            externalEntitiesEnabled = Boolean.valueOf(messageProperty.toString());
        } else {
            String message = "XML schema externalEntitiesEnabled property value is missing.";
            ThreatExceptionHandler.handleException(messageContext, message);
        }

        // Override the user defined properties if the secure XML processing is enabled
        if (isSecureXMLProcessingEnabled) {
            dtdEnabled = false;
            externalEntitiesEnabled = false;
        }

        messageProperty = messageContext.getProperty(ThreatProtectorConstants.MAX_ELEMENT_COUNT);
        if (messageProperty != null) {
            elementCount = Integer.parseInt(messageProperty.toString());
        } else {
            String message = "XML schema elementCount property value is missing.";
            ThreatExceptionHandler.handleException(messageContext, message);
        }

        messageProperty = messageContext.getProperty(ThreatProtectorConstants.MAX_ATTRIBUTE_LENGTH);
        if (messageProperty != null) {
            attributeLength = Integer.parseInt(messageProperty.toString());
        } else {
            String message = "XML schema maxAttributeLength property value is missing.";
            ThreatExceptionHandler.handleException(messageContext, message);
        }

        messageProperty = messageContext.getProperty(ThreatProtectorConstants.MAX_XML_DEPTH);
        if (messageProperty != null) {
            maxXMLDepth = Integer.parseInt(messageProperty.toString());
        } else {
            String message = "XML schema xmlDepth property value is missing.";
            ThreatExceptionHandler.handleException(messageContext, message);
        }

        messageProperty = messageContext.getProperty(ThreatProtectorConstants.MAX_ATTRIBUTE_COUNT);
        if (messageProperty != null) {
            attributeCount = Integer.parseInt(messageProperty.toString());
        } else {
            String message = "XML schema attributeCount property value is missing.";
            ThreatExceptionHandler.handleException(messageContext, message);
        }

        messageProperty = messageContext.getProperty(ThreatProtectorConstants.ENTITY_EXPANSION_LIMIT);
        if (messageProperty != null) {
            entityExpansionLimit = Integer.parseInt(messageProperty.toString());

        } else {
            String message = "XML schema entityExpansionLimit property value is missing.";
            ThreatExceptionHandler.handleException(messageContext, message);
        }

        messageProperty = messageContext.getProperty(ThreatProtectorConstants.CHILDREN_PER_ELEMENT);
        if (messageProperty == null) {
            String message = "XML schema childrenElement property value is missing.";
            ThreatExceptionHandler.handleException(messageContext, message);
        } else {
            childrenPerElement = Integer.parseInt(messageProperty.toString());
        }

        if (log.isDebugEnabled()) {
            log.debug(("DTD enable:" + dtdEnabled) + ", " + "External entities: " + externalEntitiesEnabled
                    + ", " + "Element Count:" + elementCount + ", " + "Max AttributeLength:" + attributeLength
                    + ", " + "Max xml Depth:" + maxXMLDepth + ", " + "Attribute count:" + attributeCount + ", "
                    + "Entity Expansion Limit" + attributeCount + ". " + "childrenElement:" + attributeCount);
        }
        XMLConfig xmlConfig = new XMLConfig();
        xmlConfig.setDtdEnabled(dtdEnabled);
        xmlConfig.setExternalEntitiesEnabled(externalEntitiesEnabled);
        xmlConfig.setMaxDepth(maxXMLDepth);
        xmlConfig.setMaxElementCount(elementCount);
        xmlConfig.setMaxAttributeCount(attributeCount);
        xmlConfig.setMaxAttributeLength(attributeLength);
        xmlConfig.setEntityExpansionLimit(entityExpansionLimit);
        xmlConfig.setMaxChildrenPerElement(childrenPerElement);
        return xmlConfig;
    }

    /**
     * If the isContentAware method returns false, The request message payload wont be build.
     *
     * @return isContentAware method always returns false to avoid build the message.
     */
    @Override
    public boolean isContentAware() {
        return false;
    }

    /**
     * This method validates the request payload xml with the relevant xsd.
     *
     * @param messageContext      This message context contains the request message properties of the relevant
     *                            API which was enabled the XML_Validator message mediation in flow.
     * @param bufferedInputStream Buffered input stream to be validated.
     * @throws APIMThreatAnalyzerException Exception might be occurred while parsing the xml payload.
     */
    private boolean validateSchema(MessageContext messageContext, BufferedInputStream bufferedInputStream)
            throws APIMThreatAnalyzerException {
        Object messageProperty = messageContext.getProperty(APIMgtGatewayConstants.XSD_URL);
        if (messageProperty == null || String.valueOf(messageProperty).isEmpty()) {
            return true;
        }
        String xsdURL = String.valueOf(messageProperty);
        String tenantDomain = GatewayUtils.getTenantDomain();
        RemoteUrlValidator policy = url -> APIUtil.validateRemoteURL(url, tenantDomain);
        return validateXsdAndPayload(xsdURL, policy, bufferedInputStream);
    }

    /**
     * The xsdURL gate, decoupled from the Synapse MessageContext so it is unit-testable with a mock
     * {@link RemoteUrlValidator}: (A) validate the top-level xsdURL; (B) fetch it and every nested ref
     * redirect-safely and compile the schema; (C) validate the payload with NO external resolution.
     */
    static boolean validateXsdAndPayload(String xsdURL, RemoteUrlValidator policy,
                                         BufferedInputStream bufferedInputStream)
            throws APIMThreatAnalyzerException {
        Schema schema;
        // total try: the gate only ever throws APIMThreatAnalyzerException (→ clean 400), never a 500
        try {
            // (A) gate the top-level xsdURL before any fetch
            assertXsdUrlAllowed(xsdURL, policy);

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            // (B) hardening is unconditional — not gated on EnableSecureXMLProcessing
            schemaFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            // http/https only at the JAXP layer; the resolver enforces per-host policy on nested refs
            schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "http,https");
            schemaFactory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "http,https");
            schemaFactory.setResourceResolver(new AccessControlledXmlResolver(policy));

            // compile from fetched bytes, not newSchema(URL) — a URL lets Xerces follow redirects (SSRF)
            RedirectSafeXsdFetcher.Result topLevel;
            try {
                topLevel = RedirectSafeXsdFetcher.fetch(xsdURL, policy);
            } catch (XsdRefBlockedException e) {
                throw new APIMThreatAnalyzerException(e.getMessage());
            } catch (IOException e) {
                throw new APIMThreatAnalyzerException("Error occurred while fetching the XSD : " + e);
            }

            try {
                schema = schemaFactory.newSchema(
                        new StreamSource(new ByteArrayInputStream(topLevel.body), topLevel.finalUrl));
            } catch (RuntimeException e) {
                // a wrapped block still surfaces as "not trusted"; otherwise a clean bad-request
                XsdRefBlockedException blocked = unwrapBlockedRef(e);
                if (blocked != null) {
                    throw new APIMThreatAnalyzerException(blocked.getMessage());
                }
                throw new APIMThreatAnalyzerException("Error occurred while building the XML schema : " + e);
            }

            // (C) validate the payload with external resolution disabled
            Validator validator = schema.newValidator();
            validator.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            validator.validate(new StreamSource(bufferedInputStream));
        } catch (SAXException | IOException e) {
            XsdRefBlockedException blocked = unwrapBlockedRef(e);
            if (blocked != null) {
                throw new APIMThreatAnalyzerException(blocked.getMessage());
            }
            throw new APIMThreatAnalyzerException("Error occurred while parsing XML payload : " + e);
        } catch (RuntimeException e) {
            // total fail-closed net: any other unchecked → clean 400; java.lang.Error is intentionally not caught
            XsdRefBlockedException blocked = unwrapBlockedRef(e);
            if (blocked != null) {
                throw new APIMThreatAnalyzerException(blocked.getMessage());
            }
            throw new APIMThreatAnalyzerException("Error occurred while validating against the XSD : " + e);
        }
        return true;
    }

    /**
     * Validates the top-level xsdURL against the network access-control policy. Only
     * http/https are permitted. Throws {@link APIMThreatAnalyzerException} (mapped to a
     * 400 by the mediate() handler) when the URL is blocked or uses another scheme.
     *
     * @param xsdURL the publisher-supplied schema URL.
     * @param policy the network access-control gate.
     */
    static void assertXsdUrlAllowed(String xsdURL, RemoteUrlValidator policy)
            throws APIMThreatAnalyzerException {
        if (!AccessControlledXmlResolver.isHttpOrHttps(xsdURL)) {
            throw new APIMThreatAnalyzerException(
                    "The provided XSD URL is not trusted (only HTTP/HTTPS is allowed): " + xsdURL);
        }
        try {
            policy.validate(xsdURL);
        } catch (APIManagementException e) {
            throw new APIMThreatAnalyzerException("The provided XSD URL is not trusted: " + xsdURL);
        }
    }

    /**
     * Walks the cause chain (including {@code t} itself) and returns the first
     * {@link XsdRefBlockedException}, or {@code null} if none is present. Lets the
     * mediator fail closed even when the XML parser wraps a resolver-thrown block in
     * another exception type. Bounded to avoid pathological cause cycles.
     */
    static XsdRefBlockedException unwrapBlockedRef(Throwable t) {
        Throwable cur = t;
        for (int depth = 0; cur != null && depth < 50; depth++, cur = cur.getCause()) {
            if (cur instanceof XsdRefBlockedException) {
                return (XsdRefBlockedException) cur;
            }
        }
        return null;
    }

    /**
     * Extracts the payload from the SOAP message body.
     *
     * @param axis2MC The Axis2 message context containing the SOAP message to extract payload from.
     * @return The string representation of the first element in the SOAP body.
     * @throws XMLStreamException Exception might be occurred while parsing the SOAP message or if the message format is
     *                            invalid (missing envelope, body, or first element).
     */
    private String extractPayload(org.apache.axis2.context.MessageContext axis2MC)
            throws XMLStreamException, IOException {
        try {
            RelayUtils.buildMessage(axis2MC);
            SOAPEnvelope envelope = axis2MC.getEnvelope();
            if (envelope == null) {
                logger.debug("SOAP envelope is missing in the message");
                throw new XMLStreamException(APIMgtGatewayConstants.INVALID_XML_FORMAT_MSG);
            }
            SOAPBody body = envelope.getBody();
            if (body == null) {
                logger.debug("SOAP body is missing in the message");
                throw new XMLStreamException(APIMgtGatewayConstants.INVALID_XML_FORMAT_MSG);
            }
            OMElement firstElement = body.getFirstElement();
            if (firstElement == null) {
                logger.debug("First element is missing in the SOAP body");
                throw new XMLStreamException(APIMgtGatewayConstants.INVALID_XML_FORMAT_MSG);
            }
            return firstElement.toString();

        } catch (OMException e) {
            throw new XMLStreamException(APIMgtGatewayConstants.INVALID_XML_FORMAT_MSG);
        }
    }
}
