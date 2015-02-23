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
package org.apache.synapse.mediators.xquery;

import javax.xml.xquery.*;
import net.sf.saxon.xqj.SaxonXQDataSource;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.OMText;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.dom.DOOMAbstractFactory;
import org.apache.axiom.om.util.ElementHelper;
import org.apache.synapse.MessageContext;
import org.apache.synapse.SynapseException;
import org.apache.synapse.SynapseLog;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfigUtils;
import org.apache.synapse.mediators.AbstractMediator;
import org.apache.synapse.mediators.MediatorProperty;
import org.apache.synapse.mediators.Value;
import org.apache.synapse.util.xpath.SourceXPathSupport;
import org.apache.synapse.util.xpath.SynapseXPath;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.dom.DOMSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.*;


/**
 * The XQueryMediator  provides the means to extract and manipulate data from XML documents  using
 * XQuery . It is possible to query against the current  SOAP Message or external XML. To query
 * against the current  SOAP Message ,it is need to define custom variable with any name and type as
 * element,document,document_element By providing a expression ,It is possible to select a custom
 * node for querying.The all the variable  that  have defined  in the mediator will be available
 * during the query process .Basic variable can use bind basic type.
 * currently only support * string,int,byte,short,double,long,float and boolean * types.
 * Custom Variable can use to bind XML documents ,SOAP payload and any basic type which create
 * through the XPath expression .
 */

public class XQueryMediator extends AbstractMediator {

    /* Properties that must set to the XQDataSource  */
    private final List<MediatorProperty> dataSourceProperties = new ArrayList<MediatorProperty>();

    /* The key for lookup the xquery (Supports both static and dynamic keys)*/
    private Value queryKey;

    /* The source of the xquery */
    private String querySource;

    /*The target node*/
    private final SourceXPathSupport target = new SourceXPathSupport();

    /* The list of variables for binding to the DyanamicContext in order to available for querying */
    private final List<MediatorVariable> variables = new ArrayList<MediatorVariable>();

    /*Lock used to ensure thread-safe lookup of the object from the registry */
    private final Object resourceLock = new Object();

    /* Is it need to use DOMSource and DOMResult? */
    private boolean useDOMSource = false;

    /*The DataSource which use to create a connection to XML database */
    private XQDataSource cachedXQDataSource = null;

    /* connection with a specific XQuery engine.Connection will live as long as synapse live */
    private XQConnection cachedConnection = null;

    /* An expression that use for multiple  executions.Expression will recreate if query has changed */
    private Map<String, XQPreparedExpression> cachedPreparedExpressionMap = new Hashtable<String, XQPreparedExpression>();

    public XQueryMediator() {
    }

    /**
     * Performs the query and attached the result to the target Node
     *
     * @param synCtx The current message
     * @return true always
     */
    public boolean mediate(MessageContext synCtx) {

        try {

            SynapseLog synLog = getLog(synCtx);

            if (synLog.isTraceOrDebugEnabled()) {
                synLog.traceOrDebug("Start : XQuery mediator");

                if (synLog.isTraceTraceEnabled()) {
                    synLog.traceTrace("Message : " + synCtx.getEnvelope());
                }
                synLog.traceOrDebug("Performing XQuery using query resource with key : " +
                        queryKey);
            }

            // perform the xquery
            performQuery(synCtx, synLog);

            synLog.traceOrDebug("End : XQuery mediator");

            return true;

        } catch (Exception e) {
            handleException("Unable to execute the query ", e);
        }
        return false;
    }

    /**
     * Perform the quering and get the result and attached to the target node
     *
     * @param synCtx The current MessageContext
     * @param synLog the Synapse log to use
     */
    private void performQuery(MessageContext synCtx, SynapseLog synLog) {

        boolean reLoad = false;
        boolean needBind = false;
        XQResultSequence resultSequence;
        String generatedQueryKey = null;
        boolean isQueryKeyGenerated = false;

        if (queryKey != null) {
            // Derive actual key from xpath or get static key
            generatedQueryKey = queryKey.evaluateValue(synCtx);
        }

        if (generatedQueryKey != null) {
            isQueryKeyGenerated = true;
        }

        // get expression from generatedQueryKey
        XQPreparedExpression cachedPreparedExpression = null;

        if (generatedQueryKey != null && !"".equals(generatedQueryKey)) {

            Entry dp = synCtx.getConfiguration().getEntryDefinition(generatedQueryKey);
            // if the queryKey refers to a dynamic resource
            if (dp != null && dp.isDynamic()) {
                if (!dp.isCached() || dp.isExpired()) {
                    reLoad = true;
                }
            }
        }

        try {
            synchronized (resourceLock) {

                //creating data source
                if (cachedXQDataSource == null) {
                    // A factory for XQConnection  objects
                    cachedXQDataSource = new SaxonXQDataSource();
                    //setting up the properties to the XQDataSource
                    if (dataSourceProperties != null && !dataSourceProperties.isEmpty()) {
                        synLog.traceOrDebug("Setting up properties to the XQDataSource");
                        for (MediatorProperty dataSourceProperty : dataSourceProperties) {
                            if (dataSourceProperty != null) {
                                cachedXQDataSource.setProperty(dataSourceProperty.getName(),
                                        dataSourceProperty.getValue());
                            }
                        }
                    }
                }

                //creating connection
                if (cachedConnection == null
                        || (cachedConnection != null && cachedConnection.isClosed())) {
                    //get the Connection to XML DataBase
                    synLog.traceOrDebug("Creating a connection from the XQDataSource ");
                    cachedConnection = cachedXQDataSource.getConnection();
                }

                //If already cached expression then load it from cachedPreparedExpressionMap
                if (isQueryKeyGenerated) {
                    cachedPreparedExpression = cachedPreparedExpressionMap.get(generatedQueryKey);
                }

                // prepare the expression to execute query
                if (reLoad || cachedPreparedExpression == null
                        || (cachedPreparedExpression != null
                        && cachedPreparedExpression.isClosed())) {

                    if (querySource != null && !"".equals(querySource)) {

                        if (cachedPreparedExpression == null) {

                            if (synLog.isTraceOrDebugEnabled()) {
                                synLog.traceOrDebug("Using in-lined query source - " + querySource);
                                synLog.traceOrDebug("Prepare an expression for the query ");
                            }

                            //create an XQPreparedExpression using the query source
                            cachedPreparedExpression =
                                    cachedConnection.prepareExpression(querySource);

                            // if cachedPreparedExpression is created then put it in to cachedPreparedExpressionMap
                            if (isQueryKeyGenerated) {
                                cachedPreparedExpressionMap.put(generatedQueryKey, cachedPreparedExpression);
                            }

                            // need binding because the expression just has recreated
                            needBind = true;
                        }

                    } else {

                        Object o = synCtx.getEntry(generatedQueryKey);
                        if (o == null) {
                            if (synLog.isTraceOrDebugEnabled()) {
                                synLog.traceOrDebug("Couldn't find the xquery source with a key "
                                        + queryKey);
                            }
                             throw new SynapseException("No object found for the key '" + generatedQueryKey + "'");
                        }

                        String sourceCode = null;
                        InputStream inputStream = null;
                        if (o instanceof OMElement) {
                            sourceCode = ((OMElement) (o)).getText();
                        } else if (o instanceof String) {
                            sourceCode = (String) o;
                        } else if (o instanceof OMText) {
                            DataHandler dataHandler = (DataHandler) ((OMText) o).getDataHandler();
                            if (dataHandler != null) {
                                try {
                                    inputStream = dataHandler.getInputStream();
                                    if (inputStream == null) {
                                        if (synLog.isTraceOrDebugEnabled()) {
                                            synLog.traceOrDebug("Couldn't get" +
                                                    " the stream from the xquery source with a key "
                                                    + queryKey);
                                        }
                                        return;
                                    }

                                } catch (IOException e) {
                                    handleException("Error in reading content as a stream ");
                                }
                            }
                        }

                        if ((sourceCode == null || "".equals(sourceCode)) && inputStream == null) {
                            if (synLog.isTraceOrDebugEnabled()) {
                                synLog.traceOrDebug("Couldn't find the xquery source with a key "
                                        + queryKey);
                            }
                            return;
                        }

                        if (synLog.isTraceOrDebugEnabled()) {
                            synLog.traceOrDebug("Picked up the xquery source from the " +
                                    "key " + queryKey);
                            synLog.traceOrDebug("Prepare an expression for the query ");
                        }

                        if (sourceCode != null) {
                            //create an XQPreparedExpression using the query source
                            cachedPreparedExpression =
                                    cachedConnection.prepareExpression(sourceCode);
                        } else {
                            //create an XQPreparedExpression using the query source stream
                            cachedPreparedExpression =
                                    cachedConnection.prepareExpression(inputStream);
                        }

                        // if cachedPreparedExpression is created then put it in to cachedPreparedExpressionMap
                        if (isQueryKeyGenerated) {
                            cachedPreparedExpressionMap.put(generatedQueryKey, cachedPreparedExpression);
                        }

                        // need binding because the expression just has recreated
                        needBind = true;
                    }
                }

                //Bind the external variables to the DynamicContext
                if (variables != null && !variables.isEmpty()) {
                    synLog.traceOrDebug("Binding  external variables to the DynamicContext");
                    for (MediatorVariable variable : variables) {
                        if (variable != null) {
                            boolean hasValueChanged = variable.evaluateValue(synCtx);
                            //if the value has changed or need binding because the expression has recreated
                            if (hasValueChanged || needBind) {
                                //Binds the external variable to the DynamicContext
                                bindVariable(cachedPreparedExpression, variable, synLog);
                            }
                        }
                    }
                }

                //executing the query
                resultSequence = cachedPreparedExpression.executeQuery();

            }

            if (resultSequence == null) {
                synLog.traceOrDebug("Result Sequence is null");
                return;
            }

            //processing the result 
            while (resultSequence.next()) {

                XQItem xqItem = resultSequence.getItem();
                if (xqItem == null) {
                    return;
                }
                XQItemType itemType = xqItem.getItemType();
                if (itemType == null) {
                    return;
                }
                int itemKind = itemType.getItemKind();
                int baseType = itemType.getBaseType();
                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug("The XQuery Result " + xqItem.getItemAsString(null));
                }

                //The target node that is going to modify
                OMNode destination = target.selectOMNode(synCtx, synLog);
                if (destination != null) {
                    if (synLog.isTraceOrDebugEnabled()) {
                        synLog.traceOrDebug("The target node " + destination);
                    }

                    //If the result is XML
                    if (XQItemType.XQITEMKIND_DOCUMENT_ELEMENT == itemKind ||
                            XQItemType.XQITEMKIND_ELEMENT == itemKind ||
                            XQItemType.XQITEMKIND_DOCUMENT == itemKind) {
                        StAXOMBuilder builder = new StAXOMBuilder(
                                XMLInputFactory.newInstance().createXMLStreamReader(
                                        new StringReader(xqItem.getItemAsString(null))));
                        OMElement resultOM = builder.getDocumentElement();
                        if (resultOM != null) {
                            //replace the target node from the result
                            destination.insertSiblingAfter(resultOM);
                            destination.detach();
                        }
                    } else if (XQItemType.XQBASETYPE_INTEGER == baseType ||
                            XQItemType.XQBASETYPE_INT == baseType) {
                        //replace the text value of the target node by the result ,If the result is
                        // a basic type
                        ((OMElement) destination).setText(String.valueOf(xqItem.getInt()));
                    } else if (XQItemType.XQBASETYPE_BOOLEAN == baseType) {
                        ((OMElement) destination).setText(String.valueOf(xqItem.getBoolean()));
                    } else if (XQItemType.XQBASETYPE_DOUBLE == baseType) {
                        ((OMElement) destination).setText(String.valueOf(xqItem.getDouble()));
                    } else if (XQItemType.XQBASETYPE_FLOAT == baseType) {
                        ((OMElement) destination).setText(String.valueOf(xqItem.getFloat()));
                    } else if (XQItemType.XQBASETYPE_LONG == baseType) {
                        ((OMElement) destination).setText(String.valueOf(xqItem.getLong()));
                    } else if (XQItemType.XQBASETYPE_SHORT == baseType) {
                        ((OMElement) destination).setText(String.valueOf(xqItem.getShort()));
                    } else if (XQItemType.XQBASETYPE_BYTE == baseType) {
                        ((OMElement) destination).setText(String.valueOf(xqItem.getByte()));
                    } else if (XQItemType.XQBASETYPE_STRING == baseType) {
                        ((OMElement) destination).setText(
                                String.valueOf(xqItem.getItemAsString(null)));
                    }
                }
                else if(null == target.getXPath() && null == destination){
                //In the case soap body doesn't have the first element --> Empty soap body

                     destination = synCtx.getEnvelope().getBody();

                    if (synLog.isTraceOrDebugEnabled()) {
                        synLog.traceOrDebug("The target node " + destination);
                    }

                    //If the result is XML
                    if (XQItemType.XQITEMKIND_DOCUMENT_ELEMENT == itemKind ||
                            XQItemType.XQITEMKIND_ELEMENT == itemKind ||
                            XQItemType.XQITEMKIND_DOCUMENT == itemKind) {
                        StAXOMBuilder builder = new StAXOMBuilder(
                                XMLInputFactory.newInstance().createXMLStreamReader(
                                        new StringReader(xqItem.getItemAsString(null))));
                        OMElement resultOM = builder.getDocumentElement();
                        if (resultOM != null) {
                           ((OMElement)destination).addChild(resultOM);
                        }
                    }
                    //No else part since soap body could have only XML part not text values

                }
                break;   // Only take the *first* value of the result sequence
            }
            resultSequence.close();  // closing the result sequence
        } catch (XQException e) {
            handleException("Error during the querying " + e.getMessage(), e);
        } catch (XMLStreamException e) {
            handleException("Error during retrieving  the Doument Node as  the result "
                    + e.getMessage(), e);
        }
    }

    /**
     * Binding a variable to the Dynamic Context in order to available during doing the querying
     *
     * @param xqDynamicContext The Dynamic Context  to which the variable will be binded
     * @param variable         The variable which contains the name and vaule for binding
     * @param synLog           the Synapse log to use
     * @throws XQException throws if any error occurs when binding the variable
     */
    private void bindVariable(XQDynamicContext xqDynamicContext, MediatorVariable variable,
                              SynapseLog synLog) throws XQException {

        if (variable != null) {

            QName name = variable.getName();
            int type = variable.getType();
            Object value = variable.getValue();

            if (value != null && type != -1) {

                if (synLog.isTraceOrDebugEnabled()) {
                    synLog.traceOrDebug("Binding a variable to the DynamicContext with a name : "
                            + name + " and a value : " + value);
                }

                switch (type) {
                    //Binding the basic type As-Is and XML element as an InputSource
                    case (XQItemType.XQBASETYPE_BOOLEAN): {
                        boolean booleanValue = false;
                        if (value instanceof String) {
                            booleanValue = Boolean.parseBoolean((String) value);
                        } else if (value instanceof Boolean) {
                            booleanValue = (Boolean) value;
                        } else {
                            handleException("Incompatible type for the Boolean");
                        }
                        xqDynamicContext.bindBoolean(name, booleanValue, null);
                        break;
                    }
                    case (XQItemType.XQBASETYPE_INTEGER): {
                        int intValue = -1;
                        if (value instanceof String) {
                            try {
                                intValue = Integer.parseInt((String) value);
                            } catch (NumberFormatException e) {
                                handleException("Incompatible value '" + value + "' " +
                                        "for the Integer", e);
                            }
                        } else if (value instanceof Integer) {
                            intValue = (Integer) value;
                        } else {
                            handleException("Incompatible type for the Integer");
                        }
                        if (intValue != -1) {
                            xqDynamicContext.bindInt(name, intValue, null);
                        }
                        break;
                    }
                    case (XQItemType.XQBASETYPE_INT): {
                        int intValue = -1;
                        if (value instanceof String) {
                            try {
                                intValue = Integer.parseInt((String) value);
                            } catch (NumberFormatException e) {
                                handleException("Incompatible value '" + value +
                                        "' for the Int", e);
                            }
                        } else if (value instanceof Integer) {
                            intValue = (Integer) value;
                        } else {
                            handleException("Incompatible type for the Int");
                        }
                        if (intValue != -1) {
                            xqDynamicContext.bindInt(name, intValue, null);
                        }
                        break;
                    }
                    case (XQItemType.XQBASETYPE_LONG): {
                        long longValue = -1;
                        if (value instanceof String) {
                            try {
                                longValue = Long.parseLong((String) value);
                            } catch (NumberFormatException e) {
                                handleException("Incompatible value '" + value + "' " +
                                        "for the long ", e);
                            }
                        } else if (value instanceof Long) {
                            longValue = (Long) value;
                        } else {
                            handleException("Incompatible type for the Long");
                        }
                        if (longValue != -1) {
                            xqDynamicContext.bindLong(name, longValue, null);
                        }
                        break;
                    }
                    case (XQItemType.XQBASETYPE_SHORT): {
                        short shortValue = -1;
                        if (value instanceof String) {
                            try {
                                shortValue = Short.parseShort((String) value);
                            } catch (NumberFormatException e) {
                                handleException("Incompatible value '" + value + "' " +
                                        "for the short ", e);
                            }
                        } else if (value instanceof Short) {
                            shortValue = (Short) value;
                        } else {
                            handleException("Incompatible type for the Short");
                        }
                        if (shortValue != -1) {
                            xqDynamicContext.bindShort(name, shortValue, null);
                        }
                        break;
                    }
                    case (XQItemType.XQBASETYPE_DOUBLE): {
                        double doubleValue = -1;
                        if (value instanceof String) {
                            try {
                                doubleValue = Double.parseDouble((String) value);
                            } catch (NumberFormatException e) {
                                handleException("Incompatible value '" + value + "' " +
                                        "for the double ", e);
                            }
                        } else if (value instanceof Double) {
                            doubleValue = (Double) value;
                        } else {
                            handleException("Incompatible type for the Double");
                        }
                        if (doubleValue != -1) {
                            xqDynamicContext.bindDouble(name, doubleValue, null);
                        }
                        break;
                    }
                    case (XQItemType.XQBASETYPE_FLOAT): {
                        float floatValue = -1;
                        if (value instanceof String) {
                            try {
                                floatValue = Float.parseFloat((String) value);
                            } catch (NumberFormatException e) {
                                handleException("Incompatible value '" + value + "' " +
                                        "for the float ", e);
                            }
                        } else if (value instanceof Float) {
                            floatValue = (Float) value;
                        } else {
                            handleException("Incompatible type for the Float");
                        }
                        if (floatValue != -1) {
                            xqDynamicContext.bindFloat(name, floatValue, null);
                        }
                        break;
                    }
                    case (XQItemType.XQBASETYPE_BYTE): {
                        byte byteValue = -1;
                        if (value instanceof String) {
                            try {
                                byteValue = Byte.parseByte((String) value);
                            } catch (NumberFormatException e) {
                                handleException("Incompatible value '" + value + "' " +
                                        "for the byte ", e);
                            }
                        } else if (value instanceof Byte) {
                            byteValue = (Byte) value;
                        } else {
                            handleException("Incompatible type for the Byte");
                        }
                        if (byteValue != -1) {
                            xqDynamicContext.bindByte(name, byteValue, null);
                        }
                        break;
                    }
                    case (XQItemType.XQBASETYPE_STRING): {
                        if (value instanceof String) {
                            xqDynamicContext.bindObject(name, value, null);
                        } else {
                            handleException("Incompatible type for the String");
                        }
                        break;
                    }
                    case (XQItemType.XQITEMKIND_DOCUMENT): {
                        bindOMNode(name, value, xqDynamicContext);
                        break;
                    }
                    case (XQItemType.XQITEMKIND_ELEMENT): {
                        bindOMNode(name, value, xqDynamicContext);
                        break;
                    }
                    case (XQItemType.XQITEMKIND_DOCUMENT_ELEMENT): {
                        bindOMNode(name, value, xqDynamicContext);
                        break;
                    }
                    default: {
                        handleException("Unsupported  type for the binding type" + type +
                                " in the variable name " + name);
                        break;
                    }
                }
            }
        }

    }

    private void bindOMNode(QName name, Object value,
                            XQDynamicContext xqDynamicContext) throws XQException {

        OMElement variableValue = null;
        if (value instanceof String) {
            variableValue = SynapseConfigUtils.stringToOM((String) value);
        } else if (value instanceof OMElement) {
            variableValue = (OMElement) value;
        }

        if (variableValue != null) {
            if (useDOMSource) {
                xqDynamicContext.
                        bindObject(name,
                                new DOMSource(((Element) ElementHelper.
                                        importOMElement(variableValue,
                                                DOOMAbstractFactory.getOMFactory())).
                                        getOwnerDocument()), null);
            } else {
                xqDynamicContext.bindDocument(name,SynapseConfigUtils.getInputStream(variableValue),null,null);
            }
        }
    }

    private void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }

    private void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    public Value getQueryKey() {
        return queryKey;
    }

    public void setQueryKey(Value queryKey) {
        this.queryKey = queryKey;
    }

    public String getQuerySource() {
        return querySource;
    }

    public void setQuerySource(String querySource) {
        this.querySource = querySource;
    }

    public void addAllVariables(List<MediatorVariable> list) {
        this.variables.addAll(list);
    }

    public void addVariable(MediatorVariable variable) {
        this.variables.add(variable);
    }

    public List<MediatorProperty> getDataSourceProperties() {
        return dataSourceProperties;
    }

    public List<MediatorVariable> getVariables() {
        return variables;
    }

    public SynapseXPath getTarget() {
        return target.getXPath();
    }

    public void setTarget(SynapseXPath source) {
        this.target.setXPath(source);
    }

    public void addAllDataSourceProperties(List<MediatorProperty> list) {
        this.dataSourceProperties.addAll(list);
    }

    public boolean isUseDOMSource() {
        return useDOMSource;
    }

    public void setUseDOMSource(boolean useDOMSource) {
        this.useDOMSource = useDOMSource;
    }
}
