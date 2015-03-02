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

package org.apache.synapse;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMNamespace;
import javax.xml.namespace.QName;

/**
 * Global constants for the Apache Synapse project
 */
public final class SynapseConstants {

    /** Keyword synapse */
    public static final String SYNAPSE = "synapse";
    public static final String TRUE = "TRUE";
    /** The Synapse namespace */
    public static final String SYNAPSE_NAMESPACE = "http://ws.apache.org/ns/synapse";
    /** An OMNamespace object for the Synapse NS */
    public static final OMNamespace SYNAPSE_OMNAMESPACE =
            OMAbstractFactory.getOMFactory().createOMNamespace(SYNAPSE_NAMESPACE, "");
    /** An OMNamespace object for the Empty Namespace */
    public static final OMNamespace NULL_NAMESPACE = 
    	OMAbstractFactory.getOMFactory().createOMNamespace("", "");
    /** The name of the main sequence for message mediation */
    public static final String MAIN_SEQUENCE_KEY  = "main";
    /** The associated xml file of the default main sequence */
    public static final String MAIN_SEQUENCE_XML = "main.xml";
    /** The name of the fault sequence to execute on failures during mediation */
    public static final String FAULT_SEQUENCE_KEY = "fault";
    /** The associated xml file of the default fault sequence */
    public static final String FAULT_SEQUENCE_XML = "fault.xml";
    /** The name of the mandatory sequence to execute before the begining of the actual mediation */
    public static final String MANDATORY_SEQUENCE_KEY = "pre-mediate";

    /** The name of the Synapse service (used for message mediation) */
    public static final String SYNAPSE_SERVICE_NAME ="__SynapseService";
    /** The operation name used by the Synapse service (for message mediation) */
    public static final QName SYNAPSE_OPERATION_NAME = new QName("mediate");

    //- names of modules to be engaged at runtime -
    /** The Name of the WS-RM module */
    public static final String RM_MODULE_NAME = "sandesha2";
    /** The Name of the WS-A Addressing module */
    public static final String ADDRESSING_MODULE_NAME = "addressing";
    /** The Name of the WS-Security module */
    public static final String SECURITY_MODULE_NAME = "rampart";

    //- Standard headers that can be read as get-property('header')-
    /** Refers to the To header */
    public static final String HEADER_TO = "To";
    /** Refers to the From header */
    public static final String HEADER_FROM = "From";
    /** Refers to the FaultTo header */
    public static final String HEADER_FAULT = "FaultTo";
    /** Refers to the Action header */
    public static final String HEADER_ACTION = "Action";
    /** Refers to the ReplyTo header */
    public static final String HEADER_REPLY_TO = "ReplyTo";
    /** Refers to the RelatesTo header */
    public static final String HEADER_RELATES_TO = "RelatesTo";
    /** Refers to the MessageID header */
    public static final String HEADER_MESSAGE_ID = "MessageID";
    /** Refers to the property name for which the get-property function would return
     * true, if the message is a fault
     */
    public static final String PROPERTY_FAULT = "FAULT";
    /** Message format: pox, soap11, soap12 */
    public static final String PROPERTY_MESSAGE_FORMAT = "MESSAGE_FORMAT";
    /** WSDL operation name **/
    public static final String PROPERTY_OPERATION_NAME = "OperationName";
    /** WSDL operation namespace **/
    public static final String PROPERTY_OPERATION_NAMESPACE = "OperationNamespace";
    /** System time in milliseconds - the offset from epoch (i.e. System.currentTimeMillis) */
    public static final String SYSTEM_TIME = "SYSTEM_TIME";
    /** System date */
    public static final String SYSTEM_DATE = "SYSTEM_DATE";

    public static final String ADDRESSING_VERSION_FINAL = "final";
    public static final String ADDRESSING_VERSION_SUBMISSION = "submission";

    public static final String ADDRESSING_ADDED_BY_SYNAPSE = "AddressingAddedBySynapse";

    /** The Axis2 client options property name for the Rampart service policy */
    public static final String RAMPART_POLICY = "rampartPolicy";
    /** The Axis2 client options property name for the Rampart in message policy */
    public static final String RAMPART_IN_POLICY = "rampartInPolicy";
    /** The Axis2 client options property name for the Rampart out messsage policy */
    public static final String RAMPART_OUT_POLICY = "rampartOutPolicy";
    /** The Axis2 client options property name for the Sandesha policy */
	public static final String SANDESHA_POLICY = "sandeshaPolicy";
    /** ServerManager MBean category and id */
    public static final String SERVER_MANAGER_MBEAN = "ServerManager";
    public static final String RECEIVING_SEQUENCE = "RECEIVING_SEQUENCE";

    /** Service invoked by Call mediator */
    public static final String CONTINUATION_CALL = "continuation.call";

    public static final String SYNAPSE__FUNCTION__STACK = "_SYNAPSE_FUNCTION_STACK";
    public static final String SYNAPSE_WSDL_RESOLVER = "synapse.wsdl.resolver";
    public static final String SYNAPSE_SCHEMA_RESOLVER = "synapse.schema.resolver";

    /** Parameter names in the axis2.xml that can be used to configure the synapse */
    public static final class Axis2Param {
        /** Synapse Configuration file location */
        public static final String SYNAPSE_CONFIG_LOCATION = "SynapseConfig.ConfigurationFile";
        /** Synapse Home directory */
        public static final String SYNAPSE_HOME = "SynapseConfig.HomeDirectory";
        /** Synapse resolve root */
        public static final String SYNAPSE_RESOLVE_ROOT = "SynapseConfig.ResolveRoot";
        /** Synapse server name */
        public static final String SYNAPSE_SERVER_NAME = "SynapseConfig.ServerName";
    }
    
    /** The name of the Parameter set on the Axis2Configuration to hold the Synapse Configuration */
    public static final String SYNAPSE_CONFIG = "synapse.config";
    /** The name of the Parameter set on the Axis2Configuration to hold the Synapse Environment */
    public static final String SYNAPSE_ENV = "synapse.env";
    /** The name of the Parameter set on AxisConfiguration to hold the ServerContextInformation */
    public static final String SYNAPSE_SERVER_CTX_INFO = "synapse.server.context.info";
    /** The name of the Parameter set on AxisConfiguration to hold the ServerContextInformation */
    public static final String SYNAPSE_SERVER_CONFIG_INFO = "synapse.server.config.info";

    /** The name of the system property that will hold the Synapse home directory */
    public static final String SYNAPSE_HOME = "synapse.home";
    /** The name of the system property used to specify/override the Synapse config XML location */
    public static final String SYNAPSE_XML = "synapse.xml";
    /** The name of the system property used to specify/override the Synapse properties location */
    public static final String SYNAPSE_PROPERTIES = "synapse.properties";

    /** the name of the property used for synapse library based class loading */
    public static final String SYNAPSE_LIB_LOADER = "synapse.lib.classloader";
    /** conf directory name **/
    public static final String CONF_DIRECTORY = "conf";

    // hidden service parameter
    public static final String HIDDEN_SERVICE_PARAM = "hiddenService";

    // proxy services servicetype parameter
        /** service type parameter name */
        public static final String SERVICE_TYPE_PARAM_NAME = "serviceType";
        /** service type param value for the proxy services */
        public static final String PROXY_SERVICE_TYPE = "proxy";

    //- Synapse Message Context Properties -
        /** The Synapse MC property name that holds the name of the Proxy service thats handling it */
        public static final String PROXY_SERVICE = "proxy.name";
        /** The Synapse MC property that marks it as a RESPONSE */
        public static final String RESPONSE = "RESPONSE";
        /** The Synapse MC property that indicates the in-transport */
        public static final String IN_TRANSPORT = "IN_TRANSPORT";
        /** The Synapse MC property that marks if the message was denied on the accessed transport */
        public static final String TRANSPORT_DENIED = "TRANSPORT_DENIED";
        /** The Synapse MC property that marks the message as a OUT_ONLY message */
        public static final String OUT_ONLY = "OUT_ONLY";
        /** The Synapse MC property that states that existing WS-A headers in the envelope should
        * be preserved */
        public static final String PRESERVE_WS_ADDRESSING = "PRESERVE_WS_ADDRESSING";
        /** The Synapse MC property that marks to Exception to be thrown on SOAPFault(Retry on SOAPFault)*/
        public static final String RETRY_ON_SOAPFAULT = "RETRY_ON_SOAPFAULT";

        /**
         * The name of the property which specifies the operation name that is
         * invoked by an endpoint
        */
        public static final String ENDPOINT_OPERATION = "endpoint.operation";
        /** Synapse MC property that holds the url of the named endpoint which message is sent out **/
        public static final String ENDPOINT_PREFIX = "ENDPOINT_PREFIX";

        //-- error handling --
        /** An Axis2 message context property indicating a transport send failure */
        public static final String SENDING_FAULT = "SENDING_FAULT";
        /** The message context property name which holds the error code for the last encountered exception */
        public static final String ERROR_CODE = "ERROR_CODE";
        /** The MC property name which holds the error message for the last encountered exception */
        public static final String ERROR_MESSAGE = "ERROR_MESSAGE";
        /** The message context property name which holds the error detail (stack trace) for the last encountered exception */
        public static final String ERROR_DETAIL = "ERROR_DETAIL";
        /** The message context property name which holds the exception (if any) for the last encountered exception */
        public static final String ERROR_EXCEPTION = "ERROR_EXCEPTION";
        /** The default/generic error code */
        public static final int DEFAULT_ERROR= 0;

    /** An Axis2 message context property that indicates the maximum time to spend on sending the message */
    public static final String SEND_TIMEOUT = "SEND_TIMEOUT";

    //- Axis2 Message Context Properties used by Synapse -
    /** an axis2 message context property set to hold the relates to for POX responses */
    public static final String RELATES_TO_FOR_POX = "synapse.RelatesToForPox";

    /** an axis2 message context property set to indicate this is a response message for Synapse */
    public static final String ISRESPONSE_PROPERTY = "synapse.isresponse";


    //- aspects constants -
        /** Tracing logger name */
        public static final String TRACE_LOGGER ="TRACE_LOGGER";
        public static final String SERVICE_LOGGER_PREFIX ="SERVICE_LOGGER.";

        /** The tracing state -off */
        public static final int TRACING_OFF =0;
        /** The tracing state-on */
        public static final int TRACING_ON =1;
        /** The tracing state-unset */
        public static final int TRACING_UNSET=2;

        public static final String STATISTICS_STACK ="synapse.statistics.stack";     
        
        public static final String SYNAPSE_STATISTICS_STATE = "synapse.statistics.state";
    
        public static final String SYNAPSE_ASPECT_CONFIGURATION = "synapse.aspects.configuration";

        public static final String SYNAPSE_ASPECTS ="synapse.aspects";

    //- handling of timed out events from the callbacks -
        /** The System property that states the duration at which the timeout handler runs */
        public static final String TIMEOUT_HANDLER_INTERVAL = "synapse.timeout_handler_interval";

        /**
         * Interval for activating the timeout handler for cleaning up expired requests. Note that
         * there can be an error as large as the value of the interval. But for smaller intervals
         * and larger timeouts this error is negligilble.
         */
        public static final long DEFAULT_TIMEOUT_HANDLER_INTERVAL = 15000;

        /**
         * The default endpoint suspend duration on failure (i hour)
         */
        public static final long DEFAULT_ENDPOINT_SUSPEND_TIME = 30 * 1000;

        /**
         * This is a system wide interval for handling otherwise non-expiring callbacks to
         * ensure system stability over a period of time 
         */
        public static final String GLOBAL_TIMEOUT_INTERVAL = "synapse.global_timeout_interval";

        /**
         * this is the timeout for otherwise non-expiring callbacks
         * to ensure system stability over time
         */
        public static final long DEFAULT_GLOBAL_TIMEOUT = 24 * 60 * 60 * 1000;

        /**
         * don't do anything for response timeouts. this means infinite timeout. this is the default
         * action, if the timeout configuration is not explicitly set.
         */
        public static final int NONE = 100;

        /** Discard the callback if the timeout for the response is expired */
        public static final int DISCARD = 101;

        /**
         * Discard the callback and activate specified fault sequence if the timeout for the response
         * is expired
         */
        public static final int DISCARD_AND_FAULT = 102;

        /**
         * Error codes for message sending. We go with closest HTTP fault codes.
         */
        public static final int HANDLER_TIME_OUT = 101504;

    //- Endpoints processing constants -
    /** Property name to store the last endpoint through which the message has flowed */
    public static final String LAST_ENDPOINT = "last_endpoint";

    /** Property name to store the endpoint_log that stores the history */
    public static final String ENDPOINT_LOG = "endpoint_log";     
    
    /** A name to use for anonymous endpoints */
    public static final String ANONYMOUS_ENDPOINT = "AnonymousEndpoint";

    /**A name to use for anonymous proxyservice  */
    public static final String ANONYMOUS_PROXYSERVICE = "AnonymousProxyService";

    /* Constants related to the SAL endpoints */

    public static final String PROP_SAL_ENDPOINT_FIRST_MESSAGE_IN_SESSION
            = "synapse.sal.first_message_in_session";

    public static final String PROP_SAL_ENDPOINT_ENDPOINT_LIST
            = "synapse.sal.endpoint.list";
    
    public static final String PROP_SAL_CURRENT_SESSION_INFORMATION
            = "synapse.sal.endpoint.current.sessioninformation";

    public static final String PROP_SAL_ENDPOINT_CURRENT_ENDPOINT_LIST
            = "synapse.sal.current.endpoint.list";

    public static final String PROP_SAL_ENDPOINT_CURRENT_MEMBER
            = "synapse.sal.current.member";

    public static final String PROP_SAL_ENDPOINT_CURRENT_DISPATCHER
            = "synape.sal.endpoints.dispatcher";   

    public static final String PROP_SAL_ENDPOINT_DEFAULT_SESSION_TIMEOUT
            = "synapse.sal.endpoints.sesssion.timeout.default";    

    public static final long SAL_ENDPOINTS_DEFAULT_SESSION_TIMEOUT = 120000;
    

    /** A name to use for anonymous sequences in the sequence stack */
    public static final String ANONYMOUS_SEQUENCE = "AnonymousSequence";

    /** String to be used as the separator when defining resource IDs for statistics */
    public static final String STATISTICS_KEY_SEPARATOR = "__";

    /** Message format values in EndpointDefinition. Used by address, wsdl endpoints */
    public static final String FORMAT_POX = "pox";
    public static final String FORMAT_GET = "get";
    public static final String FORMAT_SOAP11 = "soap11";
    public static final String FORMAT_SOAP12 = "soap12";    
    public static final String FORMAT_REST = "rest";

    // - Blocking Message Sender Constants
    public static final String BLOCKING_SENDER_ERROR = "blocking.sender.error";

    public static final String HTTP_SENDER_STATUSCODE = "transport.http.statusCode";

    /** Synapse server instance name */
    public static final String SERVER_NAME = "serverName";

    /** Root for relative path */
    public static final String RESOLVE_ROOT = "resolve.root";

    public static final String CLASS_MEDIATOR_LOADERS = "CLASS_MEDIATOR_LOADERS";

    /* The deployment mode */
    public static final String DEPLOYMENT_MODE = "deployment.mode";

    /* URL connection read timeout and connection timeout */

    public static final int DEFAULT_READTIMEOUT = 100000;

    public static final int DEFAULT_CONNECTTIMEOUT = 20000;

    public static final String READTIMEOUT = "synapse.connection.read_timeout";

    public static final String CONNECTTIMEOUT = "synapse.connection.connect_timeout";

    /** chunk size and chunk length configuration parameters */
    public static final int DEFAULT_THRESHOLD_CHUNKS = 8;

    public static final int DEFAULT_CHUNK_SIZE = 1024;

    public static final String DEFAULT_TEMPFILE_PREFIX = "tmp_";

    public static final String DEFAULT_TEMPFILE_SUFIX = ".dat";

    public static final String THRESHOLD_CHUNKS = "synapse.temp_data.chunk.threshold";
    
    public static final String CHUNK_SIZE = "synapse.temp_data.chunk.size";

    public static final String TEMP_FILE_PREFIX = "synapse.tempfile.prefix";
    
    public static final String TEMP_FILE_SUFIX = "synapse.tempfile.sufix";

    public static final String DOING_FAIL_OVER = "synapse.doing.failover";

    // to be a help for stat collection
    public static final String SENDING_REQUEST = "synapse.internal.request.sending";

    public static final String SYNAPSE_STARTUP_TASK_SCHEDULER = "synapse.startup.taskscheduler";

    public static final String SYNAPSE_STARTUP_TASK_DESCRIPTIONS_REPOSITORY =
            "synapse.startup.taskdescriptions.repository";

    /** proxy server configurations used for retrieving configuration resources over a HTTP proxy */
    public static final String SYNPASE_HTTP_PROXY_HOST = "synapse.http.proxy.host";
    public static final String SYNPASE_HTTP_PROXY_PORT = "synapse.http.proxy.port";
    public static final String SYNPASE_HTTP_PROXY_USER = "synapse.http.proxy.user";
    public static final String SYNPASE_HTTP_PROXY_PASSWORD = "synapse.http.proxy.password";
    public static final String SYNAPSE_HTTP_PROXY_EXCLUDED_HOSTS =
            "synapse.http.proxy.excluded.hosts";

    // host and ip synapse is running 
    public static final String SERVER_HOST = "SERVER_HOST";

    public static final String SERVER_IP = "SERVER_IP";

    // Property name. If this property is false synapse will not remove the processed headers
    public static final String PRESERVE_PROCESSED_HEADERS = "preserveProcessedHeaders";
    // Property name for preserving the envelope before sending
    public static final String PRESERVE_ENVELOPE = "PRESERVE_ENVELOPE";

    // Known transport error codes
    public static final int RCV_IO_ERROR_SENDING     = 101000;
    public static final int RCV_IO_ERROR_RECEIVING   = 101001;

    public static final int SND_IO_ERROR_SENDING     = 101500;
    public static final int SND_IO_ERROR_RECEIVING   = 101501;

    public static final int NHTTP_CONNECTION_FAILED           = 101503;
    public static final int NHTTP_CONNECTION_TIMEOUT          = 101504;
    public static final int NHTTP_CONNECTION_CLOSED           = 101505;
    public static final int NHTTP_PROTOCOL_VIOLATION          = 101506;
    public static final int NHTTP_CONNECT_CANCEL              = 101507;
    public static final int NHTTP_CONNECT_TIMEOUT             = 101508;
    public static final int NHTTP_RESPONSE_PROCESSING_FAILURE = 101510;

    // Endpoint failures
    public static final int ENDPOINT_LB_NONE_READY   = 303000;
    public static final int ENDPOINT_RL_NONE_READY   = 303000;
    public static final int ENDPOINT_FO_NONE_READY   = 303000;
    public static final int ENDPOINT_ADDRESS_NONE_READY = 303001;
    public static final int ENDPOINT_WSDL_NONE_READY = 303002;
    // Failure on endpoint in the session 
    public static final int ENDPOINT_SAL_NOT_READY = 309001;
    public static final int ENDPOINT_SAL_INVALID_PATH = 309002;
    public static final int ENDPOINT_SAL_FAILED_SESSION = 309003;

    // endpoints, non fatal warnings etc
    public static final int ENDPOINT_LB_FAIL_OVER    = 303100;
    public static final int ENDPOINT_FO_FAIL_OVER    = 304100;

    // referring real endpoint is null
    public static final int ENDPOINT_IN_DIRECT_NOT_READY = 305100;

    // callout operation failed
    public static final int CALLOUT_OPERATION_FAILED    = 401000;

    public static final String FORCE_ERROR_PROPERTY = "FORCE_ERROR_ON_SOAP_FAULT";
    public static final int ENDPOINT_CUSTOM_ERROR = 500000;


    // Fail-safe mode properties
    public static final String FAIL_SAFE_MODE_STATUS = "failsafe.mode.enable";
    public static final String FAIL_SAFE_MODE_ALL = "all";
    public static final String FAIL_SAFE_MODE_PROXY_SERVICES = "proxyservices";
    public static final String FAIL_SAFE_MODE_EP = "endpoints";
    public static final String FAIL_SAFE_MODE_LOCALENTRIES = "localentries";
    public static final String FAIL_SAFE_MODE_SEQUENCES = "sequences";
    public static final String FAIL_SAFE_MODE_EVENT_SOURCE = "eventsources";
    public static final String FAIL_SAFE_MODE_EXECUTORS = "executors";
    public static final String FAIL_SAFE_MODE_TEMPLATES = "templates";
    public static final String FAIL_SAFE_MODE_MESSAGE_PROCESSORS = "messageprocessors";
    public static final String FAIL_SAFE_MODE_MESSAGE_STORES = "messagestores";
    public static final String FAIL_SAFE_MODE_API = "api";
    public static final String FAIL_SAFE_MODE_IMPORTS = "import";
    public static final String FAIL_SAFE_MODE_TASKS = "task";
    public static final String FAIL_SAFE_MODE_REGISTRY = "registry";

    //fall back XPATH support (default javax.xml style xpath processing which can support XPATH 2.0)
    public static final String FAIL_OVER_DOM_XPATH_PROCESSING = "synapse.xpath.dom.failover.enabled";

    //Streaming XPATH Support
    public static final String STREAMING_XPATH_PROCESSING = "synapse.streaming.xpath.enabled";

    //Streaming Json Path
    public static final String STREAMING_JSONPATH_PROCESSING = "synapse.streaming.jsonpath.enabled";

    /**
     * Message content property of incoming transport-in name
     */
    public static final String TRANSPORT_IN_NAME = "TRANSPORT_IN_NAME";
    
    // Synapse config path
    public static final String SYNAPSE_CONFIGS = "synapse-configs";
    public static final String DEFAULT_DIR = "default";
    public static final String SEQUENCES_FOLDER = "sequences";

}
