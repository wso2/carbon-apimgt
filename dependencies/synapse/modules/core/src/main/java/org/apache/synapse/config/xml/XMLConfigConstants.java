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

package org.apache.synapse.config.xml;

import org.apache.axiom.om.OMNamespace;
import org.apache.synapse.SynapseConstants;

import javax.xml.namespace.QName;

/**
 * Constants used in the processing of XML configuration language
 */
public class XMLConfigConstants {

    // re-definition of the Synapse NS here to make things easier for the XML config lang code
    public static final String SYNAPSE_NAMESPACE = SynapseConstants.SYNAPSE_NAMESPACE;
    public static final OMNamespace SYNAPSE_OMNAMESPACE = SynapseConstants.SYNAPSE_OMNAMESPACE;

    //- Mediators -
    //-- PropertyMediator --
    /** The scope name for synapse message context properties */
    public static final String SCOPE_DEFAULT = "default";
    /** The scope name for synapse function template properties */
    public static final String SCOPE_FUNC = "func";
    /** The scope name for axis2 message context properties */
    public static final String SCOPE_AXIS2 = "axis2";
    /** The scope name for axis2 message context client options properties */
    public static final String SCOPE_CLIENT = "axis2-client";
    /** The scope name for transport header properties */
    public static final String SCOPE_TRANSPORT = "transport";
    /** The scope for axis2 operation **/
    public static final String SCOPE_OPERATION = "operation";
    /** The scope name for registry properties */
    public static final String SCOPE_REGISTRY = "registry";
    /** The scope name for system properties */
    public static final String SCOPE_SYSTEM = "system";
    public static final String KEY = "key";
    public static final String RECEIVE = "receive";

    /** The set of supported data types */
    public static enum DATA_TYPES {
        STRING, BOOLEAN, INTEGER, LONG, SHORT, FLOAT, DOUBLE, OM
    }

    //-- WS-RM sequence mediator --
    /** WS-RM version 1.0*/
    public static final String SEQUENCE_VERSION_1_0 = "1.0";
    /** WS-RM version 1.1*/
    public static final String SEQUENCE_VERSION_1_1 = "1.1";

    //- configuration language constants -
    public static final QName DEFINITIONS_ELT = new QName(SYNAPSE_NAMESPACE, "definitions");
    public static final QName DESCRIPTION_ELT = new QName(SYNAPSE_NAMESPACE, "description");
    public static final QName SEQUENCE_ELT    = new QName(SYNAPSE_NAMESPACE, "sequence");
    public static final QName TEMPLATE_ELT    = new QName(SYNAPSE_NAMESPACE, "template");
    public static final QName IMPORT_ELT    = new QName(SYNAPSE_NAMESPACE, "import");
    public static final QName ENDPOINT_ELT    = new QName(SYNAPSE_NAMESPACE, "endpoint");
    public static final QName ENTRY_ELT       = new QName(SYNAPSE_NAMESPACE, "localEntry");
    public static final QName REGISTRY_ELT    = new QName(SYNAPSE_NAMESPACE, "registry");
    public static final QName PROXY_ELT       = new QName(SYNAPSE_NAMESPACE, "proxy");
    public static final QName EVENT_SOURCE_ELT = new QName(SYNAPSE_NAMESPACE, "eventSource");
    public static final QName MESSAGE_STORE_ELT = new QName(SYNAPSE_NAMESPACE, "messageStore");
    public static final QName MESSAGE_PROCESSOR_ELT = new QName(SYNAPSE_NAMESPACE, "messageProcessor");
    public static final QName API_ELT = new QName(SYNAPSE_NAMESPACE, "api");

    public static final String NULL_NAMESPACE = "";
    public static final Object QUARTZ_QNAME   =
        new QName("http://www.opensymphony.com/quartz/JobSchedulingData", "quartz");
    public static final QName EXECUTOR_ELT = new QName(SYNAPSE_NAMESPACE, "priorityExecutor");

	/** The Trace attribute name, for proxy services, sequences */
	public static final String TRACE_ATTRIB_NAME = "trace";
	/** The Trace value 'enable' */
	public static final String TRACE_ENABLE = "enable";
	/** The Trace value 'disable' */
	public static final String TRACE_DISABLE = "disable";

	/** The statistics attribute name */
	public static final String STATISTICS_ATTRIB_NAME = "statistics";
	/** The statistics value 'enable' */
	public static final String STATISTICS_ENABLE = "enable";
	/** The statistics value 'disable' */
	public static final String STATISTICS_DISABLE = "disable";

    public static final String SUSPEND_ON_FAILURE = "suspendOnFailure";
    public static final String SUSPEND_INITIAL_DURATION = "initialDuration";
    public static final String SUSPEND_PROGRESSION_FACTOR = "progressionFactor";
    public static final String SUSPEND_MAXIMUM_DURATION = "maximumDuration";
    public static final String ERROR_CODES = "errorCodes";
    public static final String MARK_FOR_SUSPENSION = "markForSuspension";
    public static final String RETRIES_BEFORE_SUSPENSION = "retriesBeforeSuspension";
    public static final String RETRY_DELAY = "retryDelay";

    public static final String RETRY_CONFIG = "retryConfig";

	public static final String LOADBALANCE_POLICY = "policy";
	public static final String LOADBALANCE_ALGORITHM = "algorithm";

    //TODO FIX-RUWAN
    public static final String ALGORITHM_NAME = "policy";

    public static final String ONREJECT = "onReject";
	public static final String ONACCEPT = "onAccept";

    public static final QName ATT_XPATH_RELATIVE = new QName("relative");
    public static final String XPATH_BODY_RELATIVE = "body";
    public static final String XPATH_ENVELOPE_RELATIVE = "envelope";
    
    public static final String CONFIG_REF = "configKey";
}
