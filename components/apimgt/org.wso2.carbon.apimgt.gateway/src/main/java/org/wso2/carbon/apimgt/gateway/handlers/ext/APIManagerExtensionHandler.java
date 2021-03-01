/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.gateway.handlers.ext;


import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.rest.AbstractHandler;
import org.apache.synapse.rest.RESTConstants;
import org.wso2.carbon.apimgt.gateway.APIMgtGatewayConstants;
import org.wso2.carbon.apimgt.gateway.MethodStats;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.tracing.TracingSpan;
import org.wso2.carbon.apimgt.tracing.TracingTracer;
import org.wso2.carbon.apimgt.tracing.Util;
import org.wso2.carbon.metrics.manager.MetricManager;
import org.wso2.carbon.metrics.manager.Timer;

import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * A simple extension handler for the APIs deployed in the API gateway. This handler first
 * looks for a sequence named WSO2AM--Ext--[Dir], where [Dir] could be either In or Out
 * depending on the direction of the message. If such a sequence is found, it is invoked.
 * Following that a more API specific extension sequence is looked up by using the name
 * pattern provider--api--version--[Dir]. If such an API specific sequence is found, that
 * is also invoked. If no extension is found either at the global level or at the per API level
 * this mediator simply returns true.
 */
public class APIManagerExtensionHandler extends AbstractHandler {

    private static final String EXT_SEQUENCE_PREFIX = "WSO2AM--Ext--";
    private static final String DIRECTION_IN = "In";
    private static final String DIRECTION_OUT = "Out";
    private static final Log log = LogFactory.getLog(APIManagerExtensionHandler.class);

    @MethodStats
    public boolean mediate(MessageContext messageContext, String direction) {
        // In order to avoid a remote registry call occurring on each invocation, we
        // directly get the extension sequences from the local registry.
        Map localRegistry = messageContext.getConfiguration().getLocalRegistry();

        Object sequence = localRegistry.get(EXT_SEQUENCE_PREFIX + direction);
        if (sequence instanceof Mediator) {
            if (!((Mediator) sequence).mediate(messageContext)) {
                return false;
            }
        }

        String apiName = (String) messageContext.getProperty(RESTConstants.SYNAPSE_REST_API);
        sequence = localRegistry.get(apiName + "--" + direction);
        if (sequence instanceof Mediator) {
            return ((Mediator) sequence).mediate(messageContext);
        }
        return true;
    }

    @MethodStats
    public boolean handleRequest(MessageContext messageContext) {
        // reset HTTP_METHOD, to rest api values before send them to analytics.
        // (only for graphQL APIs)
        if (messageContext.getProperty(APIConstants.API_TYPE) != null && APIConstants.GRAPHQL_API
                .equals(messageContext.getProperty(APIConstants.API_TYPE).toString())) {
            ((Axis2MessageContext) messageContext).getAxis2MessageContext().
                    setProperty(Constants.Configuration.HTTP_METHOD,
                            messageContext.getProperty(APIConstants.HTTP_VERB));
        }

        Timer.Context context = startMetricTimer(DIRECTION_IN);
        long executionStartTime = System.nanoTime();
        TracingSpan requestMediationSpan = null;
        if (Util.tracingEnabled()) {
            TracingSpan responseLatencySpan =
                    (TracingSpan) messageContext.getProperty(APIMgtGatewayConstants.RESPONSE_LATENCY);
            TracingTracer tracer = Util.getGlobalTracer();
            requestMediationSpan = Util.startSpan(APIMgtGatewayConstants.REQUEST_MEDIATION, responseLatencySpan, tracer);
        }
        try {
            boolean isMediated = mediate(messageContext, DIRECTION_IN);
            if (isMediated) {
                String requestDestination = null;
                EndpointReference objectTo = ((Axis2MessageContext) messageContext).getAxis2MessageContext()
                        .getOptions().getTo();
                if (objectTo != null) {
                    requestDestination = objectTo.getAddress();
                }
                if (requestDestination != null) {
                    messageContext.setProperty(APIMgtGatewayConstants.SYNAPSE_ENDPOINT_ADDRESS, requestDestination);
                }
            }
            return isMediated;
        } catch (Exception e) {
            if (Util.tracingEnabled() && requestMediationSpan != null) {
                Util.setTag(requestMediationSpan, APIMgtGatewayConstants.ERROR,
                        APIMgtGatewayConstants.REQUEST_MEDIATION_ERROR);
            }
            throw e;
        } finally {
            if (Util.tracingEnabled()) {
                Util.finishSpan(requestMediationSpan);
            }
            messageContext.setProperty(APIMgtGatewayConstants.REQUEST_MEDIATION_LATENCY,
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - executionStartTime));
            stopMetricTimer(context);
        }
    }

    @MethodStats
    public boolean handleResponse(MessageContext messageContext) {
        Timer.Context context = startMetricTimer(DIRECTION_OUT);
        long executionStartTime = System.nanoTime();
        TracingSpan responseMediationSpan = null;
        if (Util.tracingEnabled()) {
            TracingSpan responseLatencySpan =
                    (TracingSpan) messageContext.getProperty(APIMgtGatewayConstants.RESPONSE_LATENCY);
            TracingTracer tracer = Util.getGlobalTracer();
            responseMediationSpan =
                    Util.startSpan(APIMgtGatewayConstants.RESPONSE_MEDIATION, responseLatencySpan, tracer);
        }
        try {
            return mediate(messageContext, DIRECTION_OUT);
        } catch (Exception e) {
            if (Util.tracingEnabled() && responseMediationSpan != null) {
                Util.setTag(responseMediationSpan, APIMgtGatewayConstants.ERROR,
                        APIMgtGatewayConstants.RESPONSE_MEDIATION_ERROR);
            }
            throw e;
        } finally {
            if (Util.tracingEnabled()) {
                Util.finishSpan(responseMediationSpan);
            }
            messageContext.setProperty(APIMgtGatewayConstants.RESPONSE_MEDIATION_LATENCY,
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - executionStartTime));
            stopMetricTimer(context);
        }
    }

	protected void stopMetricTimer(Timer.Context context) {
		context.stop();
	}

	protected Timer.Context startMetricTimer(String direction) {
		Timer timer = MetricManager.timer(org.wso2.carbon.metrics.manager.Level.INFO,
				MetricManager.name(APIConstants.METRICS_PREFIX, this.getClass().getSimpleName(), direction));
		return timer.start();
	}
}

