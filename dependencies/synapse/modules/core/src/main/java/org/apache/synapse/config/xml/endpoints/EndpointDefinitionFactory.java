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

package org.apache.synapse.config.xml.endpoints;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.SynapseConstants;
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.xml.XMLConfigConstants;
import org.apache.synapse.endpoints.EndpointDefinition;

import javax.xml.namespace.QName;
import java.util.StringTokenizer;

public class EndpointDefinitionFactory implements DefinitionFactory{
    public static final Log log = LogFactory.getLog(EndpointDefinitionFactory.class);

    /**
     * Extracts the QoS information from the XML which represents a WSDL/Address/Default endpoints
     *
     * @param elem XML which represents the endpoint with QoS information
     * @return the created endpoint definition
     */
    public EndpointDefinition createDefinition(OMElement elem) {
        EndpointDefinition definition = new EndpointDefinition();

        OMAttribute optimize
                = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE, "optimize"));
        OMAttribute encoding
                = elem.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE, "encoding"));

        OMAttribute trace = elem.getAttribute(new QName(
                XMLConfigConstants.NULL_NAMESPACE, XMLConfigConstants.TRACE_ATTRIB_NAME));
        if (trace != null && trace.getAttributeValue() != null) {
            String traceValue = trace.getAttributeValue();
            if (XMLConfigConstants.TRACE_ENABLE.equals(traceValue)) {
                definition.setTraceState(SynapseConstants.TRACING_ON);
            } else if (XMLConfigConstants.TRACE_DISABLE.equals(traceValue)) {
                definition.setTraceState(SynapseConstants.TRACING_OFF);
            }
        } else {
            definition.setTraceState(SynapseConstants.TRACING_UNSET);
        }


        if (optimize != null && optimize.getAttributeValue().length() > 0) {
            String method = optimize.getAttributeValue().trim();
            if ("mtom".equalsIgnoreCase(method)) {
                definition.setUseMTOM(true);
            } else if ("swa".equalsIgnoreCase(method)) {
                definition.setUseSwa(true);
            }
        }

        if (encoding != null && encoding.getAttributeValue() != null) {
            definition.setCharSetEncoding(encoding.getAttributeValue());
        }

        OMElement wsAddr = elem.getFirstChildWithName(
                new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "enableAddressing"));
        if (wsAddr != null) {

            definition.setAddressingOn(true);

            OMAttribute version = wsAddr.getAttribute(new QName("version"));
            if (version != null && version.getAttributeValue() != null) {
                String versionValue = version.getAttributeValue().trim().toLowerCase();
                if (SynapseConstants.ADDRESSING_VERSION_FINAL.equals(versionValue) ||
                        SynapseConstants.ADDRESSING_VERSION_SUBMISSION.equals(versionValue)) {
                    definition.setAddressingVersion(version.getAttributeValue());
                } else {
                    handleException("Unknown value for the addressing version. Possible values " +
                            "for the addressing version are 'final' and 'submission' only.");
                }
            }

            String useSepList = wsAddr.getAttributeValue(new QName("separateListener"));
            if (useSepList != null) {
                if ("true".equals(useSepList.trim().toLowerCase())) {
                    definition.setUseSeparateListener(true);
                }
            }
        }

        OMElement wsSec = elem.getFirstChildWithName(
                new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "enableSec"));
        if (wsSec != null) {

            definition.setSecurityOn(true);

            OMAttribute policyKey      = wsSec.getAttribute(
                    new QName(XMLConfigConstants.NULL_NAMESPACE, "policy"));
            OMAttribute inboundPolicyKey  = wsSec.getAttribute(
                    new QName(XMLConfigConstants.NULL_NAMESPACE, "inboundPolicy"));
            OMAttribute outboundPolicyKey = wsSec.getAttribute(
                    new QName(XMLConfigConstants.NULL_NAMESPACE, "outboundPolicy"));

            if (policyKey != null && policyKey.getAttributeValue() != null) {
                definition.setWsSecPolicyKey(policyKey.getAttributeValue());
            } else {
                if (inboundPolicyKey != null && inboundPolicyKey.getAttributeValue() != null) {
                    definition.setInboundWsSecPolicyKey(inboundPolicyKey.getAttributeValue());
                }
                if (outboundPolicyKey != null && outboundPolicyKey.getAttributeValue() != null) {
                    definition.setOutboundWsSecPolicyKey(outboundPolicyKey.getAttributeValue());
                }
            }
        }

        OMElement wsRm = elem.getFirstChildWithName(
                new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "enableRM"));
        if (wsRm != null) {

            definition.setReliableMessagingOn(true);

            OMAttribute policy
                    = wsRm.getAttribute(new QName(XMLConfigConstants.NULL_NAMESPACE, "policy"));
            if (policy != null) {
                definition.setWsRMPolicyKey(policy.getAttributeValue());
            }
        }

        // set the timeout configuration
        OMElement timeout = elem.getFirstChildWithName(
                new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "timeout"));
        if (timeout != null) {
            OMElement duration = timeout.getFirstChildWithName(
                    new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "duration"));

            if (duration != null) {
                String d = duration.getText();
                if (d != null) {
                    try {
                        long timeoutMilliSeconds = Long.parseLong(d.trim());
                        definition.setTimeoutDuration(timeoutMilliSeconds);
                    } catch (NumberFormatException e) {
                        handleException("Endpoint timeout duration expected as a " +
                                "number but was not a number");
                    }
                }
            }

            OMElement action = timeout.getFirstChildWithName(
                    new QName(XMLConfigConstants.SYNAPSE_NAMESPACE, "responseAction"));
            if (action != null && action.getText() != null) {
                String actionString = action.getText();
                if ("discard".equalsIgnoreCase(actionString.trim())) {

                    definition.setTimeoutAction(SynapseConstants.DISCARD);

                    // set timeout duration to 30 seconds, if it is not set explicitly
                    if (definition.getTimeoutDuration() == 0) {
                        definition.setTimeoutDuration(30000);
                    }
                } else if ("fault".equalsIgnoreCase(actionString.trim())) {

                    definition.setTimeoutAction(SynapseConstants.DISCARD_AND_FAULT);

                    // set timeout duration to 30 seconds, if it is not set explicitly
                    if (definition.getTimeoutDuration() == 0) {
                        definition.setTimeoutDuration(30000);
                    }
                } else {
                    handleException("Invalid timeout action, action : "
                            + actionString + " is not supported");
                }
            }
        }

        OMElement markAsTimedOut = elem.getFirstChildWithName(new QName(
            SynapseConstants.SYNAPSE_NAMESPACE,
            XMLConfigConstants.MARK_FOR_SUSPENSION));

        if (markAsTimedOut != null) {

            OMElement timeoutCodes = markAsTimedOut.getFirstChildWithName(new QName(
                SynapseConstants.SYNAPSE_NAMESPACE,
                XMLConfigConstants.ERROR_CODES));
            if (timeoutCodes != null && timeoutCodes.getText() != null) {
                StringTokenizer st = new StringTokenizer(timeoutCodes.getText().trim(), ", ");
                while (st.hasMoreTokens()) {
                    String s = st.nextToken();
                    try {
                        definition.addTimeoutErrorCode(Integer.parseInt(s));
                    } catch (NumberFormatException e) {
                        handleException("The timeout error codes should be specified " +
                            "as valid numbers separated by commas : " + timeoutCodes.getText(), e);
                    }
                }
            }

            OMElement retriesBeforeSuspend = markAsTimedOut.getFirstChildWithName(new QName(
                SynapseConstants.SYNAPSE_NAMESPACE,
                XMLConfigConstants.RETRIES_BEFORE_SUSPENSION));
            if (retriesBeforeSuspend != null && retriesBeforeSuspend.getText() != null) {
                try {
                    definition.setRetriesOnTimeoutBeforeSuspend(
                        Integer.parseInt(retriesBeforeSuspend.getText().trim()));
                } catch (NumberFormatException e) {
                    handleException("The retries before suspend [for timeouts] should be " +
                        "specified as a valid number : " + retriesBeforeSuspend.getText(), e);
                }
            }

            OMElement retryDelay = markAsTimedOut.getFirstChildWithName(new QName(
                SynapseConstants.SYNAPSE_NAMESPACE,
                XMLConfigConstants.RETRY_DELAY));
            if (retryDelay != null && retryDelay.getText() != null) {
                try {
                    definition.setRetryDurationOnTimeout(
                        Integer.parseInt(retryDelay.getText().trim()));
                } catch (NumberFormatException e) {
                    handleException("The retry delay for timeouts should be specified " +
                        "as a valid number : " + retryDelay.getText(), e);
                }
            }
        }

        // support backwards compatibility with Synapse 1.2 - for suspendDurationOnFailure
        OMElement suspendDurationOnFailure = elem.getFirstChildWithName(new QName(
            SynapseConstants.SYNAPSE_NAMESPACE, "suspendDurationOnFailure"));
        if (suspendDurationOnFailure != null && suspendDurationOnFailure.getText() != null) {

            log.warn("Configuration uses deprecated style for endpoint 'suspendDurationOnFailure'");
            try {
                definition.setInitialSuspendDuration(
                        1000 * Long.parseLong(suspendDurationOnFailure.getText().trim()));
                definition.setSuspendProgressionFactor((float) 1.0);
            } catch (NumberFormatException e) {
                handleException("The initial suspend duration should be specified " +
                    "as a valid number : " + suspendDurationOnFailure.getText(), e);
            }
        }

        OMElement suspendOnFailure = elem.getFirstChildWithName(new QName(
            SynapseConstants.SYNAPSE_NAMESPACE,
            XMLConfigConstants.SUSPEND_ON_FAILURE));

        if (suspendOnFailure != null) {

            OMElement suspendCodes = suspendOnFailure.getFirstChildWithName(new QName(
                SynapseConstants.SYNAPSE_NAMESPACE,
                XMLConfigConstants.ERROR_CODES));
            if (suspendCodes != null && suspendCodes.getText() != null) {

                StringTokenizer st = new StringTokenizer(suspendCodes.getText().trim(), ", ");
                while (st.hasMoreTokens()) {
                    String s = st.nextToken();
                    try {
                        definition.addSuspendErrorCode(Integer.parseInt(s));
                    } catch (NumberFormatException e) {
                        handleException("The suspend error codes should be specified " +
                            "as valid numbers separated by commas : " + suspendCodes.getText(), e);
                    }
                }
            }

            OMElement initialDuration = suspendOnFailure.getFirstChildWithName(new QName(
                SynapseConstants.SYNAPSE_NAMESPACE,
                XMLConfigConstants.SUSPEND_INITIAL_DURATION));
            if (initialDuration != null && initialDuration.getText() != null) {
                try {
                    definition.setInitialSuspendDuration(
                        Integer.parseInt(initialDuration.getText().trim()));
                } catch (NumberFormatException e) {
                    handleException("The initial suspend duration should be specified " +
                        "as a valid number : " + initialDuration.getText(), e);
                }
            }

            OMElement progressionFactor = suspendOnFailure.getFirstChildWithName(new QName(
                SynapseConstants.SYNAPSE_NAMESPACE,
                XMLConfigConstants.SUSPEND_PROGRESSION_FACTOR));
            if (progressionFactor != null && progressionFactor.getText() != null) {
                try {
                    definition.setSuspendProgressionFactor(
                        Float.parseFloat(progressionFactor.getText().trim()));
                } catch (NumberFormatException e) {
                    handleException("The suspend duration progression factor should be specified " +
                        "as a valid float : " + progressionFactor.getText(), e);
                }
            }

            OMElement maximumDuration = suspendOnFailure.getFirstChildWithName(new QName(
                SynapseConstants.SYNAPSE_NAMESPACE,
                XMLConfigConstants.SUSPEND_MAXIMUM_DURATION));
            if (maximumDuration != null && maximumDuration.getText() != null) {
                try {
                    definition.setSuspendMaximumDuration(
                        Long.parseLong(maximumDuration.getText().trim()));
                } catch (NumberFormatException e) {
                    handleException("The maximum suspend duration should be specified " +
                        "as a valid number : " + maximumDuration.getText(), e);
                }
            }
        }

        OMElement retryConfig = elem.getFirstChildWithName(new QName(
            SynapseConstants.SYNAPSE_NAMESPACE, XMLConfigConstants.RETRY_CONFIG));

        if (retryConfig != null) {

            OMElement retryDisabledErrorCodes = retryConfig.getFirstChildWithName(new QName(
                SynapseConstants.SYNAPSE_NAMESPACE, "disabledErrorCodes"));
            if (retryDisabledErrorCodes != null && retryDisabledErrorCodes.getText() != null) {

                StringTokenizer st = new StringTokenizer(
                        retryDisabledErrorCodes.getText().trim(), ", ");
                while (st.hasMoreTokens()) {
                    String s = st.nextToken();
                    try {
                        definition.addRetryDisabledErrorCode(Integer.parseInt(s));
                    } catch (NumberFormatException e) {
                        handleException("The suspend error codes should be specified as valid " +
                                "numbers separated by commas : "
                                + retryDisabledErrorCodes.getText(), e);
                    }
                }
            }

            OMElement retryEnabledErrorCodes = retryConfig.getFirstChildWithName(new QName(
                    SynapseConstants.SYNAPSE_NAMESPACE, "enabledErrorCodes"));
            if (retryEnabledErrorCodes != null && retryEnabledErrorCodes.getText() != null) {

                StringTokenizer st = new StringTokenizer(
                        retryEnabledErrorCodes.getText().trim(), ", ");
                while (st.hasMoreTokens()) {
                    String s = st.nextToken();
                    try {
                        definition.addRetryEnabledErrorCode(Integer.parseInt(s));
                    } catch (NumberFormatException e) {
                        handleException("The suspend error codes should be specified as valid " +
                                "numbers separated by commas : "
                                + retryEnabledErrorCodes.getText(), e);
                    }
                }
            }



        }

        return definition;
    }

    protected static void handleException(String msg) {
        log.error(msg);
        throw new SynapseException(msg);
    }

    protected static void handleException(String msg, Exception e) {
        log.error(msg, e);
        throw new SynapseException(msg, e);
    }
}
