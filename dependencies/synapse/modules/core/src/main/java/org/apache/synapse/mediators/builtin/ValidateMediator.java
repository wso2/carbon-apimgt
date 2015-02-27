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

package org.apache.synapse.mediators.builtin;

import org.apache.axiom.om.OMNode;
import org.apache.synapse.ContinuationState;
import org.apache.synapse.FaultHandler;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.continuation.ContinuationStackManager;
import org.apache.synapse.mediators.AbstractListMediator;
import org.apache.synapse.mediators.FlowContinuableMediator;
import org.apache.synapse.mediators.MediatorProperty;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.util.AXIOMUtils;
import org.apache.synapse.util.jaxp.SchemaResourceResolver;
import org.apache.synapse.util.resolver.ResourceMap;
import org.apache.synapse.util.resolver.UserDefinedXmlSchemaURIResolver;
import org.apache.synapse.util.xpath.SourceXPathSupport;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Validate a message or an element against a schema
 * <p/>
 * This internally uses the Xerces2-j parser, which cautions a lot about thread-safety and
 * memory leaks. Hence this initial implementation will create a single parser instance
 * for each unique mediator instance, and re-use it to validate multiple messages - even
 * concurrently - by synchronizing access
 */
public class ValidateMediator extends AbstractListMediator implements FlowContinuableMediator {

    /**
     * A list of property keys, referring to the schemas to be used for the validation
     * key can be static or dynamic(xpath) key
     */
    private List<Value> schemaKeys = new ArrayList<Value>();

    /**
     * A list of property keys, referring to the external schema resources to be used for the validation
     */
    private ResourceMap resourceMap;

    /**
     * An XPath expression to be evaluated against the message to find the element to be validated.
     * If this is not specified, the validation will occur against the first child element of the
     * SOAP body
     */
    private final SourceXPathSupport source = new SourceXPathSupport();

    /**
     * A Map containing features to be passed to the actual validator (Xerces)
     */
    private final List<MediatorProperty> explicityFeatures = new ArrayList<MediatorProperty>();

    /**
     * This is the actual schema instance used to create a new schema
     * This is a thred-safe instance.
     */
    private Schema cachedSchema;

    /**
     * Lock used to ensure thread-safe creation and use of the above Validator
     */
    private final Object validatorLock = new Object();

    /**
     * The SchemaFactory used to create new schema instances.
     */
    private final SchemaFactory factory = SchemaFactory.newInstance(
            XMLConstants.W3C_XML_SCHEMA_NS_URI);

    @SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
    public boolean mediate(MessageContext synCtx) {

        SynapseLog synLog = getLog(synCtx);

        synLog.traceOrDebug("Start : Validate mediator");
        if (synLog.isTraceTraceEnabled()) {
            synLog.traceTrace("Message : " + synCtx.getEnvelope());
        }

        // Input source for the validation
        Source validateSrc = getValidationSource(synCtx, synLog);

        // flag to check if we need to initialize/re-initialize the schema
        boolean reCreate = false;
        // if any of the schemas are not loaded, or have expired, load or re-load them
        for (Value schemaKey : schemaKeys) {
            // Derive actual key from message context
            String propKey = schemaKey.evaluateValue(synCtx);
            Entry dp = synCtx.getConfiguration().getEntryDefinition(propKey);
            if (dp != null && dp.isDynamic()) {
                if (!dp.isCached() || dp.isExpired()) {
                    reCreate = true;       // request re-initialization of Validator
                }
            }
        }

        // This is the reference to the DefaultHandler instance
        ValidateMediatorErrorHandler errorHandler = new ValidateMediatorErrorHandler();

        // do not re-initialize schema unless required
        synchronized (validatorLock) {
            if (reCreate || cachedSchema == null) {

                factory.setErrorHandler(errorHandler);
                StreamSource[] sources = new StreamSource[schemaKeys.size()];
                int i = 0;
                for (Value schemaKey : schemaKeys) {
                    // Derive actual key from message context
                    String propName = schemaKey.evaluateValue(synCtx);
                    sources[i++] = SynapseConfigUtils.getStreamSource(synCtx.getEntry(propName));
                }
                // load the UserDefined SchemaURIResolver implementations
                try {
                	SynapseConfiguration synCfg = synCtx.getConfiguration();
                	if(synCfg.getProperty(SynapseConstants.SYNAPSE_SCHEMA_RESOLVER) !=null){
                		setUserDefinedSchemaResourceResolver(synCtx);
                	}
                	else{
                		factory.setResourceResolver(
                		                            new SchemaResourceResolver(synCtx.getConfiguration(), resourceMap));
                	}
                    cachedSchema = factory.newSchema(sources);
                } catch (SAXException e) {
                    handleException("Error creating a new schema objects for " +
                            "schemas : " + schemaKeys.toString(), e, synCtx);
                } catch (RuntimeException e) {
                    handleException("Error creating a new schema objects for " +
                            "schemas : " + schemaKeys.toString(), e, synCtx);
                }

                if (errorHandler.isValidationError()) {
                    //reset the errorhandler state
                    errorHandler.setValidationError(false);
                    cachedSchema = null;
                    handleException("Error creating a new schema objects for schemas : "
                            + schemaKeys.toString(), errorHandler.getSaxParseException(), synCtx);
                }
            }
        }

        // no need to synchronize, schema instances are thread-safe
        try {
            Validator validator = cachedSchema.newValidator();
            validator.setErrorHandler(errorHandler);

            // perform actual validation
            validator.validate(validateSrc);

            if (errorHandler.isValidationError()) {

                if (synLog.isTraceOrDebugEnabled()) {
                    String msg = "Validation of element returned by XPath : " + source +
                        " failed against the given schema(s) " + schemaKeys +
                        "with error : " + errorHandler.getSaxParseException().getMessage() +
                        " Executing 'on-fail' sequence";
                    synLog.traceOrDebug(msg);

                    // write a warning to the service log
                    synCtx.getServiceLog().warn(msg);

                    if (synLog.isTraceTraceEnabled()) {
                        synLog.traceTrace("Failed message envelope : " + synCtx.getEnvelope());
                    }
                }

                // set error message and detail (stack trace) into the message context
                synCtx.setProperty(SynapseConstants.ERROR_MESSAGE,
                    errorHandler.getSaxParseException().getMessage());
                synCtx.setProperty(SynapseConstants.ERROR_EXCEPTION,
                    errorHandler.getSaxParseException());
                synCtx.setProperty(SynapseConstants.ERROR_DETAIL,
                    FaultHandler.getStackTrace(errorHandler.getSaxParseException()));

                // super.mediate() invokes the "on-fail" sequence of mediators
                ContinuationStackManager.addReliantContinuationState(synCtx, 0, getMediatorPosition());
                boolean result = super.mediate(synCtx);
                if (result) {
                    ContinuationStackManager.removeReliantContinuationState(synCtx);
                }
                return result;
            }
        } catch (SAXException e) {
            handleException("Error validating " + source + " element", e, synCtx);
        } catch (IOException e) {
            handleException("Error validating " + source + " element", e, synCtx);
        }

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Validation of element returned by the XPath expression : "
                + source + " succeeded against the given schemas and the current message");
            synLog.traceOrDebug("End : Validate mediator");
        }

        return true;
    }

    public boolean mediate(MessageContext synCtx,
                           ContinuationState continuationState) {

        SynapseLog synLog = getLog(synCtx);

        if (synLog.isTraceOrDebugEnabled()) {
            synLog.traceOrDebug("Validate mediator : Mediating from ContinuationState");
        }

        boolean result;
        if (!continuationState.hasChild()) {
            result = super.mediate(synCtx, continuationState.getPosition() + 1);
        } else {
            FlowContinuableMediator mediator =
                    (FlowContinuableMediator) getChild(continuationState.getPosition());
            result = mediator.mediate(synCtx, continuationState.getChildContState());
        }
        return result;
    }

    /**
     * UserDefined schema resource resolver

     * @param synCtx message context
     */
    private void setUserDefinedSchemaResourceResolver(MessageContext synCtx) {
        SynapseConfiguration synCfg = synCtx.getConfiguration();
        String schemaResolverName = synCfg.getProperty(SynapseConstants.SYNAPSE_SCHEMA_RESOLVER);
        Class schemaClazz;
        Object schemaClazzObject;
        try {
            schemaClazz = Class.forName(schemaResolverName);
        } catch (ClassNotFoundException e) {
            String msg =
                    "System could not find the class defined for the specific properties" +
                            "\n SchemaResolverImplementation:" + schemaResolverName;
            handleException(msg, e, synCtx);
            return;
        }

        try {
            schemaClazzObject = schemaClazz.newInstance();

            UserDefinedXmlSchemaURIResolver userDefSchemaResResolver =
                    (UserDefinedXmlSchemaURIResolver) schemaClazzObject;
            userDefSchemaResResolver.init(resourceMap, synCfg, schemaKeys);
            factory.setResourceResolver(userDefSchemaResResolver);
        } catch (Exception e) {
            String msg = "Could not create an instance from the class";
            handleException(msg, e, synCtx);
        }
    }
    
    /**
     * Get the validation Source for the message context
     *
     * @param synCtx the current message to validate
     * @param synLog  SynapseLog instance
     * @return the validation Source for the current message
     */
    private Source getValidationSource(MessageContext synCtx, SynapseLog synLog) {

        try {
            OMNode validateSource = source.selectOMNode(synCtx, synLog);
            if (synLog.isTraceOrDebugEnabled()) {
                synLog.traceOrDebug("Validation source : " + validateSource.toString());
            }

            return AXIOMUtils.asSource(validateSource);

        } catch (Exception e) {
            handleException("Error accessing source element : " + source, e, synCtx);
        }
        return null; // never reaches here
    }

    /**
     * This class handles validation errors to be used for the error reporting
     */
    private static class ValidateMediatorErrorHandler extends DefaultHandler {

        private boolean validationError = false;
        private SAXParseException saxParseException = null;

        public void error(SAXParseException exception) throws SAXException {
            validationError = true;
            saxParseException = exception;
        }

        public void fatalError(SAXParseException exception) throws SAXException {
            validationError = true;
            saxParseException = exception;
        }

        public void warning(SAXParseException exception) throws SAXException {
        }

        public boolean isValidationError() {
            return validationError;
        }

        public SAXParseException getSaxParseException() {
            return saxParseException;
        }

        /**
         * To set explicitly validation error condition
         * @param validationError  is occur validation error?
         */
        public void setValidationError(boolean validationError) {
            this.validationError = validationError;
        }
    }

    // setters and getters

    /**
     * Get a mediator feature. The common use case is a feature for the
     * underlying Xerces validator
     *
     * @param key property key / feature name
     * @return property string value (usually true|false)
     */
    public Object getFeature(String key) {
        for (MediatorProperty prop : explicityFeatures) {
            if (key.equals(prop.getName())) {
                return prop.getValue();
            }
        }
        return null;
    }

    /**
     * add a feature which need to set for the Schema Factory
     *
     * @param  featureName The name of the feature
     * @param isFeatureEnable should this feature enable?(true|false)
     * @see #getFeature(String)
     * @throws SAXException on an unknown feature
     */
   public void addFeature(String featureName, boolean isFeatureEnable) throws SAXException {
        MediatorProperty mp = new MediatorProperty();
        mp.setName(featureName);
        if (isFeatureEnable) {
            mp.setValue("true");
        } else {
            mp.setValue("false");
        }
        explicityFeatures.add(mp);
        factory.setFeature(featureName, isFeatureEnable);
    }

    /**
     * Set a list of local property names which refer to a list of schemas to be
     * used for validation
     *
     * @param schemaKeys list of local property names
     */
    public void setSchemaKeys(List<Value> schemaKeys) {
        this.schemaKeys = schemaKeys;
    }

    /**
     * Set the given XPath as the source XPath
     * @param source an XPath to be set as the source
     */
    public void setSource(SynapseXPath source) {
       this.source.setXPath(source);
    }

    /**
     * Set the External Schema ResourceMap that will required for schema validation
     * @param resourceMap  the ResourceMap which contains external schema resources
     */
    public void setResourceMap(ResourceMap resourceMap) {
        this.resourceMap = resourceMap;
    }

    /**
     * Get the source XPath which yields the source element for validation
     * @return the XPath which yields the source element for validation
     */
    public SynapseXPath getSource() {
        return source.getXPath();
    }

    /**
     * The keys for the schema resources used for validation
     * @return schema registry keys
     */
    public List<Value> getSchemaKeys() {
        return schemaKeys;
    }

    /**
     * Features for the actual Xerces validator
     * @return explicityFeatures to be passed to the Xerces validator
     */
    public List<MediatorProperty> getFeatures() {
        return explicityFeatures;
    }

    /**
     *ResourceMap for the external schema resources to be used for the validation
     * @return the ResourceMap with external schema resources
     */
    public ResourceMap getResourceMap() {
        return resourceMap;
    }

    @Override
    public boolean isContentAware() {
        return true;
    }

}
