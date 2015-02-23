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

package org.apache.synapse.transport.nhttp;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Locale;
import java.util.Properties;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.AxisFault;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.TransportInDescription;
import org.apache.axis2.transport.TransportListener;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.evaluators.EvaluatorConstants;
import org.apache.synapse.commons.evaluators.EvaluatorException;
import org.apache.synapse.commons.evaluators.Parser;
import org.apache.synapse.commons.executors.ExecutorConstants;
import org.apache.synapse.commons.executors.PriorityExecutor;
import org.apache.synapse.commons.executors.config.PriorityExecutorFactory;

class ListenerContextBuilder {
    
    private final Log log = LogFactory.getLog(ListenerContextBuilder.class);

    private final TransportInDescription transportIn;
    private final String name;

    private String host = "localhost";
    private int port = 8280;
    private PriorityExecutor executor = null;
    private Parser parser = null;
    private boolean restDispatching = true;
    private HttpGetRequestProcessor httpGetRequestProcessor = null;
    private InetAddress bindAddress;
    
    public ListenerContextBuilder(final TransportInDescription transportIn) {
        this.transportIn = transportIn;
        this.name = transportIn.getName().toUpperCase(Locale.US);
    }

    public ListenerContextBuilder parse() throws AxisFault {
        Parameter param = transportIn.getParameter(TransportListener.HOST_ADDRESS);
        if (param != null) {
            host = ((String) param.getValue()).trim();
        } else {
            try {
                host = InetAddress.getLocalHost().getHostName();
            } catch (UnknownHostException e) {
                log.warn("Unable to lookup local host name, using 'localhost'");
            }
        }
        
        param = transportIn.getParameter(TransportListener.PARAM_PORT);
        if (param != null) {
            port = Integer.parseInt((String) param.getValue());
        }

        int portOffset = 0;

        try {
            portOffset = Integer.parseInt(System.getProperty(NhttpConstants.PORT_OFFSET, "0"));
        } catch (NumberFormatException e) {
            handleException("portOffset System property should be a valid Integer", e);
        }

        port = port + portOffset;

        if (param != null) {
            param.setValue(String.valueOf(port));
            param.getParameterElement().setText(String.valueOf(port));
        }

        param = transportIn.getParameter(NhttpConstants.BIND_ADDRESS);
        if (param != null) {
            String s = ((String) param.getValue()).trim();
            try {
                bindAddress = InetAddress.getByName(s);
            } catch (UnknownHostException ex) {
                throw AxisFault.makeFault(ex);
            }
        }
        
        // create the priority based executor and parser
        param = transportIn.getParameter(NhttpConstants.PRIORITY_CONFIG_FILE_NAME);
        if (param != null && param.getValue() != null) {
            String fileName = param.getValue().toString();
            OMElement definitions = null;
            try {
                FileInputStream fis = new FileInputStream(fileName);
                definitions = new StAXOMBuilder(fis).getDocumentElement();
                definitions.build();
            } catch (FileNotFoundException e) {
                handleException("Priority configuration file cannot be found : " + fileName, e);
            } catch (XMLStreamException e) {
                handleException("Error parsing priority configuration xml file " + fileName, e);
            }
            
            executor = createPriorityExecutor(definitions);
            parser = createParser(definitions);
            
            if (log.isInfoEnabled()) {
                log.info(name + " Created a priority based executor from the configuration: " +
                    fileName);
            }
        }

        param = transportIn.getParameter(NhttpConstants.DISABLE_REST_SERVICE_DISPATCHING);
        if (param != null && param.getValue() != null) {
            if (param.getValue().equals("true")) {
                restDispatching = false;
            }
        }

        // create http Get processor
        param = transportIn.getParameter(NhttpConstants.HTTP_GET_PROCESSOR);
        if (param != null && param.getValue() != null) {
            httpGetRequestProcessor = createHttpGetProcessor(param.getValue().toString());
            if (httpGetRequestProcessor == null) {
                handleException("Cannot create HttpGetRequestProcessor");
            }
        } else {
            httpGetRequestProcessor = new DefaultHttpGetProcessor();
        }
        return this;
    }
    
    private PriorityExecutor createPriorityExecutor(final OMElement definitions) throws AxisFault {
        assert definitions != null;
        OMElement executorElem = definitions.getFirstChildWithName(
                new QName(ExecutorConstants.PRIORITY_EXECUTOR));

        if (executorElem == null) {
            handleException(ExecutorConstants.PRIORITY_EXECUTOR +
                    " configuration is mandatory for priority based routing");
        }

        PriorityExecutor executor = PriorityExecutorFactory.createExecutor(
                null, executorElem, false, new Properties());
        executor.init();
        return executor;
    }

    private Parser createParser(final OMElement definitions) throws AxisFault {
        OMElement conditionsElem = definitions.getFirstChildWithName(
            new QName(EvaluatorConstants.CONDITIONS));
        if (conditionsElem == null) {
            handleException("Conditions configuration is mandatory for priority based routing");
        }
    
        assert conditionsElem != null;
        OMAttribute defPriorityAttr = conditionsElem.getAttribute(
                new QName(EvaluatorConstants.DEFAULT_PRIORITY));
        Parser parser;
        if (defPriorityAttr != null) {
            parser = new Parser(Integer.parseInt(defPriorityAttr.getAttributeValue()));
        } else {
            parser = new Parser();
        }
    
        try {
            parser.init(conditionsElem);
        } catch (EvaluatorException e) {
            handleException("Invalid " + EvaluatorConstants.CONDITIONS +
                    " configuration for priority based mediation", e);
        }
        return parser;
    }
    
    private HttpGetRequestProcessor createHttpGetProcessor(String str) throws AxisFault {
        Object obj = null;
        try {
            obj = Class.forName(str).newInstance();
        } catch (ClassNotFoundException e) {
            handleException("Error creating WSDL processor", e);
        } catch (InstantiationException e) {
            handleException("Error creating WSDL processor", e);
        } catch (IllegalAccessException e) {
            handleException("Error creating WSDL processor", e);
        }

        if (obj instanceof HttpGetRequestProcessor) {
            return (HttpGetRequestProcessor) obj;
        } else {
            handleException("Error creating WSDL processor. The HttpProcessor should be of type " +
                    "org.apache.synapse.transport.nhttp.HttpGetRequestProcessor");
        }

        return null;
    }

    private void handleException(String msg, Exception e) throws AxisFault {
        log.error(name + " " + msg, e);
        throw new AxisFault(msg, e);
    }

    private void handleException(String msg) throws AxisFault {
        log.error(name + " " + msg);
        throw new AxisFault(msg);
    }

    public ListenerContext build() throws AxisFault {
        return new ListenerContext(
            transportIn, executor, parser, restDispatching, 
            httpGetRequestProcessor,  host, port, bindAddress);
    }

}